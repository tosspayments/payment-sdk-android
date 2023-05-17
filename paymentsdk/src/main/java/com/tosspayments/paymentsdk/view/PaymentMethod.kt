package com.tosspayments.paymentsdk.view

import android.content.Context
import android.util.AttributeSet
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo

class PaymentMethod(context: Context, attrs: AttributeSet? = null) :
    PaymentWidgetContainer(context, attrs) {

    companion object {
        internal const val EVENT_NAME_CUSTOM_REQUESTED = "customRequest"
        internal const val EVENT_NAME_CUSTOM_METHOD_SELECTED = "customPaymentMethodSelect"
        internal const val EVENT_NAME_CUSTOM_METHOD_UNSELECTED = "customPaymentMethodUnselect"

        internal const val MESSAGE_NOT_RENDERED = "PaymentMethod is not rendered. Call 'renderPaymentMethods' method first."
    }
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
            throw IllegalArgumentException(MESSAGE_NOT_RENDERED)
        }
    }

    @JvmOverloads
    @Throws(IllegalAccessException::class)
    internal fun updateAmount(amount: Number, description: String = "") {
        if (methodRenderCalled) {
            evaluateJavascript("paymentMethods.updateAmount(${amount}, '${description}');")
        } else {
            throw IllegalArgumentException(MESSAGE_NOT_RENDERED)
        }
    }
}