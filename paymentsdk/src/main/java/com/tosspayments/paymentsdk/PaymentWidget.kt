package com.tosspayments.paymentsdk

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.tosspayments.paymentsdk.activity.TossPaymentsWebActivity
import com.tosspayments.paymentsdk.interfaces.PaymentWidgetCallback
import com.tosspayments.paymentsdk.model.Constants
import com.tosspayments.paymentsdk.model.PaymentWidgetOptions
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.view.PaymentMethodWidget

class PaymentWidget(
    activity: AppCompatActivity,
    private val clientKey: String,
    private val customerKey: String,
    private val options: PaymentWidgetOptions? = null
) {
    private val tossPayments: TossPayments = TossPayments(clientKey)

    private val redirectUrl = options?.brandPayOption?.redirectUrl

    private var methodWidget: PaymentMethodWidget? = null
    private var requestCode: Int? = null
    private var paymentResultLauncher: ActivityResultLauncher<Intent>? = null

    private val htmlRequestActivityResult =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                methodWidget?.evaluateJavascript(
                    result.data?.getStringExtra(Constants.EXTRA_KEY_DATA).orEmpty()
                )
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
            override fun onPostPaymentHtml(html: String, domain: String?) {
                handlePaymentDom(orderId, html, domain)
            }

            override fun onHtmlRequested(html: String, domain: String?) {
                methodWidget?.context?.let { context ->
                    htmlRequestActivityResult.launch(
                        TossPaymentsWebActivity.getIntent(context, domain, html)
                    )
                }
            }

            override fun onSuccess(response: String, domain: String?) {
            }
        }
    }

    private fun handlePaymentDom(orderId: String, paymentHtml: String, domain: String? = null) {
        methodWidget?.context?.let { context ->
            if (paymentHtml.isNotBlank()) {
                when {
                    requestCode != null -> {
                        tossPayments.requestPayment(
                            context = context,
                            paymentHtml = paymentHtml,
                            orderId = orderId,
                            requestCode = requestCode!!,
                            domain = domain
                        )
                    }
                    paymentResultLauncher != null -> {
                        tossPayments.requestPayment(
                            context = context,
                            paymentHtml = paymentHtml,
                            orderId = orderId,
                            paymentResultLauncher = paymentResultLauncher!!,
                            domain = domain
                        )
                    }
                }
            }
        }
    }

    fun setMethodWidget(methodWidget: PaymentMethodWidget) {
        this.methodWidget = methodWidget
    }

    fun renderPaymentMethodWidget(amount: Number, orderId: String) {
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
                customerName,
                redirectUrl
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
        customerName: String? = null
    ) {
        methodWidget?.let {
            this.requestCode = requestCode

            it.requestPayment(
                orderId,
                orderName,
                customerEmail,
                customerName,
                redirectUrl
            )
        } ?: kotlin.run {
            this.requestCode = null
            throw IllegalAccessException("Payment method widget is not set")
        }
    }
}