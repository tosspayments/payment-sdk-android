package com.tosspayments.paymentsdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.tosspayments.paymentsdk.activity.TossPaymentActivity
import com.tosspayments.paymentsdk.model.TossPayment
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.model.paymentinfo.*

class TossPayments(private val clientKey: String) : TossPayment {
    companion object {
        internal const val EXTRA_PAYMENT_INFO = "extraPaymentInfo"
        internal const val EXTRA_CLIENT_KEY = "extraClientKey"
        internal const val EXTRA_METHOD = "extraMethod"
        internal const val EXTRA_PAYMENT_DOM = "extraPaymentDom"
        internal const val EXTRA_ORDER_ID = "extraOrderId"

        const val EXTRA_PAYMENT_RESULT_SUCCESS = "extraPaymentResultSuccess"
        const val EXTRA_PAYMENT_RESULT_FAILED = "extraPaymentResultFailed"

        const val RESULT_PAYMENT_SUCCESS: Int = 200
        const val RESULT_PAYMENT_FAILED: Int = 201

        @JvmStatic
        fun getPaymentResultLauncher(
            activity: AppCompatActivity,
            onSuccess: (TossPaymentResult.Success) -> Unit,
            onFailed: (TossPaymentResult.Fail) -> Unit
        ): ActivityResultLauncher<Intent> {
            return activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                handlePaymentResult(result, onSuccess, onFailed)
            }
        }

        private fun handlePaymentResult(
            result: ActivityResult,
            onSuccess: (TossPaymentResult.Success) -> Unit,
            onFailed: (TossPaymentResult.Fail) -> Unit
        ) {
            when (result.resultCode) {
                RESULT_PAYMENT_SUCCESS -> {
                    result.data?.getParcelableExtra<TossPaymentResult.Success>(
                        EXTRA_PAYMENT_RESULT_SUCCESS
                    )?.let { success ->
                        onSuccess.invoke(success)
                    }
                }
                RESULT_PAYMENT_FAILED -> {
                    result.data?.getParcelableExtra<TossPaymentResult.Fail>(
                        EXTRA_PAYMENT_RESULT_FAILED
                    )?.let { fail ->
                        onFailed.invoke(fail)
                    }
                }
                else -> {}
            }
        }
    }

    override fun requestPayment(
        context: Context,
        paymentHtml: String,
        orderId: String,
        paymentResultLauncher: ActivityResultLauncher<Intent>,
        domain: String?
    ) {
        paymentResultLauncher.launch(
            TossPaymentActivity.getIntent(context, paymentHtml, orderId, domain)
        )
    }

    override fun requestPayment(
        context: Context,
        paymentHtml: String,
        orderId: String,
        requestCode: Int,
        domain: String?
    ) {
        (context as? Activity)?.startActivityForResult(
            TossPaymentActivity.getIntent(context, paymentHtml, orderId, domain),
            requestCode
        )
    }

    override fun requestPayment(
        activity: Activity,
        method: TossPaymentMethod,
        paymentInfo: TossPaymentInfo,
        paymentResultLauncher: ActivityResultLauncher<Intent>
    ) {
        paymentResultLauncher.launch(
            TossPaymentActivity.getIntent(activity, clientKey, method, paymentInfo)
        )
    }

    override fun requestPayment(
        activity: Activity,
        method: TossPaymentMethod,
        paymentInfo: TossPaymentInfo,
        requestCode: Int
    ) {
        activity.startActivityForResult(
            TossPaymentActivity.getIntent(activity, clientKey, method, paymentInfo),
            requestCode
        )
    }

    override fun requestCardPayment(
        activity: Activity,
        paymentInfo: TossCardPaymentInfo,
        paymentResultLauncher: ActivityResultLauncher<Intent>
    ) {
        requestPayment(activity, TossPaymentMethod.Card, paymentInfo, paymentResultLauncher)
    }

    override fun requestCardPayment(
        activity: Activity,
        paymentInfo: TossCardPaymentInfo,
        requestCode: Int
    ) {
        requestPayment(activity, TossPaymentMethod.Card, paymentInfo, requestCode)
    }

    override fun requestAccountPayment(
        activity: Activity,
        paymentInfo: TossAccountPaymentInfo,
        paymentResultLauncher: ActivityResultLauncher<Intent>
    ) {
        requestPayment(activity, TossPaymentMethod.Account, paymentInfo, paymentResultLauncher)
    }

    override fun requestAccountPayment(
        activity: Activity,
        paymentInfo: TossAccountPaymentInfo,
        requestCode: Int
    ) {
        requestPayment(activity, TossPaymentMethod.Account, paymentInfo, requestCode)
    }

    override fun requestTransferPayment(
        activity: Activity,
        paymentInfo: TossTransferPaymentInfo,
        paymentResultLauncher: ActivityResultLauncher<Intent>
    ) {
        requestPayment(activity, TossPaymentMethod.Transfer, paymentInfo, paymentResultLauncher)
    }

    override fun requestTransferPayment(
        activity: Activity,
        paymentInfo: TossTransferPaymentInfo,
        requestCode: Int
    ) {
        requestPayment(activity, TossPaymentMethod.Transfer, paymentInfo, requestCode)
    }

    override fun requestMobilePayment(
        activity: Activity,
        paymentInfo: TossMobilePaymentInfo,
        paymentResultLauncher: ActivityResultLauncher<Intent>
    ) {
        requestPayment(activity, TossPaymentMethod.Mobile, paymentInfo, paymentResultLauncher)
    }

    override fun requestMobilePayment(
        activity: Activity,
        paymentInfo: TossMobilePaymentInfo,
        requestCode: Int
    ) {
        requestPayment(activity, TossPaymentMethod.Mobile, paymentInfo, requestCode)
    }

    override fun requestGiftCertificatePayment(
        activity: Activity,
        method: TossPaymentMethod.GiftCertificate,
        paymentInfo: TossPaymentInfo,
        paymentResultLauncher: ActivityResultLauncher<Intent>
    ) {
        requestPayment(activity, method, paymentInfo, paymentResultLauncher)
    }

    override fun requestGiftCertificatePayment(
        activity: Activity,
        method: TossPaymentMethod.GiftCertificate,
        paymentInfo: TossPaymentInfo,
        requestCode: Int
    ) {
        requestPayment(activity, method, paymentInfo, requestCode)
    }
}