package com.tosspayments.paymentsdk.view

import android.content.Context
import android.util.AttributeSet
import com.tosspayments.paymentsdk.model.paymentinfo.EscrowProduct
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentMobileCarrier
import org.json.JSONArray
import org.json.JSONObject

class PaymentMethod(context: Context, attrs: AttributeSet? = null) :
    PaymentWidgetContainer(context, attrs) {
    internal var orderId: String = ""
        private set

    companion object {
        internal const val EVENT_NAME_CUSTOM_REQUESTED = "customRequest"
        internal const val EVENT_NAME_CUSTOM_METHOD_SELECTED = "customPaymentMethodSelect"
        internal const val EVENT_NAME_CUSTOM_METHOD_UNSELECTED = "customPaymentMethodUnselect"

        internal const val MESSAGE_NOT_RENDERED =
            "PaymentMethod is not rendered. Call 'renderPaymentMethods' method first."
    }

    internal fun renderPaymentMethods(
        clientKey: String,
        customerKey: String,
        amount: Number,
        domain: String? = null,
        redirectUrl: String? = null
    ) {
        renderWidget(clientKey, customerKey, domain, redirectUrl) {
            appendLine("const paymentMethods = paymentWidget.renderPaymentMethods('#payment-method', $amount);")
        }
    }

    @Throws(IllegalAccessException::class)
    internal fun requestPayment(paymentInfo: PaymentInfo, redirectUrl: String?) {
        if (methodRenderCalled) {
            this.orderId = paymentInfo.orderId

            val requestPaymentPayload = paymentInfo.getPayload().put(
                "redirectUrl",
                JSONObject().put("brandpay", JSONObject().put("redirectUrl", redirectUrl))
            )

            evaluateJavascript("paymentWidget.requestPaymentForNativeSDK($requestPaymentPayload)")
        } else {
            this.orderId = ""
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

    data class PaymentInfo(
        val orderId: String,
        val orderName: String
    ) : TossPaymentInfo(orderId, orderName, 0) {
        var taxExemptionAmount: Number = 0
        var useEscrow: Boolean? = null
        var escrowProducts: List<EscrowProduct>? = null
        var customerMobilePhone: String? = null
        var showCustomerMobilePhone: Boolean = false
        var mobileCarrier: List<TossPaymentMobileCarrier>? = null

        override val paymentPayload: JSONObject.(JSONObject) -> JSONObject
            get() = {
                val escrowProductsPayload = JSONArray()
                escrowProducts?.map { it.json }?.forEach {
                    escrowProductsPayload.put(it)
                }
                put("escrowProducts", escrowProductsPayload)

                remove("amount")
                put("taxExemptionAmount", taxExemptionAmount)
                put("useEscrow", useEscrow ?: false)
                put("customerMobilePhone", customerMobilePhone.orEmpty())
                put("showCustomerMobilePhone", showCustomerMobilePhone)

                mobileCarrier?.let {
                    put("mobileCarrier", JSONArray().apply {
                        it.forEach { code ->
                            this.put(code)
                        }
                    })
                }

                this
            }
    }
}