package com.tosspayments.paymentsdk.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo

@SuppressLint("SetJavaScriptEnabled")
class PaymentMethodWidget(context: Context, attrs: AttributeSet? = null) :
    PaymentWidgetContainer(context, attrs) {
    internal fun renderPaymentMethods(
        clientKey: String,
        customerKey: String,
        amount: Number,
        domain: String? = null,
        redirectUrl: String? = null
    ) {
        renderWidget(clientKey, customerKey, domain, redirectUrl) {
            appendLine("paymentWidget.renderPaymentMethods('#payment-method', $amount);")
        }
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
    internal fun updateAmount(amount: Number, description: String = "") {
        if (methodRenderCalled) {
            evaluateJavascript("paymentMethods.updateAmount(${amount}, '${description}');")
        } else {
            throw IllegalArgumentException("renderPaymentMethods method should be called before the payment requested.")
        }
    }
}