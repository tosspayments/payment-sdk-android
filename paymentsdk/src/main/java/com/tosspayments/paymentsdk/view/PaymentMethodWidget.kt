package com.tosspayments.paymentsdk.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo

@SuppressLint("SetJavaScriptEnabled")
class PaymentMethodWidget(context: Context, attrs: AttributeSet? = null) :
    PaymentWidgetContainer(context, attrs) {
    private var methodRenderCalled = false

    private val redirectOption: (redirectUrl: String?) -> String? = {
        it?.let { redirectUrl -> "{'brandpay':{'redirectUrl':'$redirectUrl'}}" }
    }

    internal fun renderPaymentMethods(
        clientKey: String,
        customerKey: String,
        amount: Number,
        domain: String? = null,
        redirectUrl: String? = null
    ) {
        val paymentWidgetConstructor =
            "PaymentWidget('$clientKey', '$customerKey', ${redirectOption(redirectUrl)})"

        val renderMethodScript = StringBuilder()
            .appendLine("var paymentWidget = $paymentWidgetConstructor;")
            .appendLine("paymentWidget.renderPaymentMethods('#payment-method', $amount);")
            .toString()

        paymentWebView.loadHtml(
            domain,
            "tosspayment_widget.html",
            {
                evaluateJavascript(renderMethodScript)
                methodRenderCalled = true
            },
            {
                handleOverrideUrl(this)
            }
        )
    }

    @Throws(IllegalAccessException::class)
    internal fun requestPayment(
        orderId: String,
        orderName: String,
        customerEmail: String?,
        customerName: String?,
        redirectUrl: String?
    ) {
        if (methodRenderCalled) {
            val requestPaymentScript = StringBuilder().apply {
                appendLine("paymentWidget.requestPaymentForNativeSDK({")
                appendLine("orderId: '${orderId}',")
                appendLine("orderName: '${orderName}',")
                appendLine("successUrl: '${TossPaymentInfo.successUri}',")
                appendLine("failUrl: '${TossPaymentInfo.failUri}',")
                appendLine("customerEmail: '${customerEmail.orEmpty()}',")
                appendLine("customerName: '${customerName.orEmpty()}',")
                appendLine("redirectUrl : ${redirectOption(redirectUrl)}})")
            }.toString()

            evaluateJavascript(requestPaymentScript)
        } else {
            throw IllegalArgumentException("renderPaymentMethods method should be called before the payment requested.")
        }
    }

    @JvmOverloads
    fun updateAmount(amount: Number, description: String = "") {

    }
}