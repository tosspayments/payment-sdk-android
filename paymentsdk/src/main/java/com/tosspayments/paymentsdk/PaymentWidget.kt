package com.tosspayments.paymentsdk

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.tosspayments.paymentsdk.activity.TossPaymentsWebActivity
import com.tosspayments.paymentsdk.interfaces.PaymentWidgetCallback
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.view.PaymentMethodWidget

class PaymentWidget(
    activity: AppCompatActivity,
    private val clientKey: String,
    private val customerKey: String
) {
    private val tossPayments: TossPayments = TossPayments(clientKey)

    private var methodWidget: PaymentMethodWidget? = null
    private var requestCode: Int? = null
    private var paymentResultLauncher: ActivityResultLauncher<Intent>? = null

    private val htmlRequestActivityResult =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d("Kangdroid", "data")
            }
        }

    companion object {
        @JvmStatic
        fun getPaymentResultLauncher(
            activity: AppCompatActivity,
            onSuccess: (TossPaymentResult.Success) -> Unit,
            onFailed: (TossPaymentResult.Fail) -> Unit
        ): ActivityResultLauncher<Intent> {
            return TossPayments.getPaymentResultLauncher(activity, onSuccess, onFailed)
        }
    }

    private fun getPaymentWidgetCallback(orderId: String): PaymentWidgetCallback {
        return object : PaymentWidgetCallback {
            override fun onPaymentDomCreated(paymentDom: String) {
                handlePaymentDom(orderId, paymentDom)
            }

            override fun onHtmlRequested(html: String) {
                methodWidget?.context?.let { context ->
                    htmlRequestActivityResult.launch(
                        TossPaymentsWebActivity.getIntent(context, html)
                    )
                }
            }

            override fun onHtmlRequestSucceeded(html: String) {
            }
        }
    }

    private fun handlePaymentDom(orderId: String, paymentDom: String) {
        methodWidget?.context?.let {
            if (paymentDom.isNotBlank()) {
                when {
                    requestCode != null -> {
                        tossPayments.requestPayment(
                            it,
                            paymentDom,
                            orderId,
                            requestCode!!
                        )
                    }
                    paymentResultLauncher != null -> {
                        tossPayments.requestPayment(
                            it,
                            paymentDom,
                            orderId,
                            paymentResultLauncher!!
                        )
                    }
                }
            }
        }
    }

    fun setMethodWidget(methodWidget: PaymentMethodWidget) {
        this.methodWidget = methodWidget
    }

    fun renderPaymentMethodWidget(amount: Number, orderId: String, redirectUrl: String) {
        methodWidget?.renderPaymentMethods(
            clientKey,
            customerKey,
            amount,
            redirectUrl,
            getPaymentWidgetCallback(orderId)
        )
    }

    @JvmOverloads
    @Throws(IllegalAccessException::class)
    fun requestPayment(
        paymentResultLauncher: ActivityResultLauncher<Intent>,
        orderId: String,
        orderName: String,
        customerEmail: String? = null,
        customerName: String? = null
    ) {
        methodWidget?.let {
            this.paymentResultLauncher = paymentResultLauncher

            it.requestPayment(
                orderId,
                orderName,
                customerEmail,
                customerName
            )
        } ?: kotlin.run {
            this.paymentResultLauncher = null
            throw IllegalAccessException("Payment method widget is not set")
        }
    }

    @JvmOverloads
    @Throws(IllegalAccessException::class)
    fun requestPayment(
        requestCode: Int,
        orderId: String,
        orderName: String,
        customerEmail: String? = null,
        customerName: String? = null,
    ) {
        methodWidget?.let {
            this.requestCode = requestCode

            it.requestPayment(
                orderId,
                orderName,
                customerEmail,
                customerName
            )
        } ?: kotlin.run {
            this.requestCode = null
            throw IllegalAccessException("Payment method widget is not set")
        }
    }
}