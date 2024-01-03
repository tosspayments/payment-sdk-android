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

    override val widgetName: String
        get() = "paymentMethods"

    companion object {
        internal const val EVENT_NAME_CUSTOM_REQUESTED = "customRequest"
        internal const val EVENT_NAME_CUSTOM_METHOD_SELECTED = "customPaymentMethodSelect"
        internal const val EVENT_NAME_CUSTOM_METHOD_UNSELECTED = "customPaymentMethodUnselect"
        internal const val EVENT_NAME_CHANGE_PAYMENT_METHOD = "changePaymentMethod"

        internal const val MESSAGE_NOT_RENDERED =
            "PaymentMethod is not rendered. Call 'renderPaymentMethods' method first."
    }

    internal fun renderPaymentMethods(
        clientKey: String,
        customerKey: String,
        amount: Rendering.Amount,
        options: Rendering.Options? = null,
        domain: String? = null,
        redirectUrl: String? = null
    ) {
        val amountJson = JSONObject().apply {
            put("value", amount.value)
            put("currency", amount.currency.name)
            put("country", amount.country)
        }

        val optionsJson = JSONObject().apply {
            options?.let {
                put("variantKey", it.variantKey)
            }
        }

        val renderMethodScript =
            "const paymentMethods = paymentWidget.renderPaymentMethods('#payment-method', $amountJson, $optionsJson);"

        renderWidget(clientKey, customerKey, domain, redirectUrl) {
            appendLine(renderMethodScript)
        }
    }

    @Throws(IllegalAccessException::class)
    internal fun requestPayment(paymentInfo: PaymentInfo) {
        if (methodRenderCalled) {
            this.orderId = paymentInfo.orderId
            val requestPaymentPayload = paymentInfo.getPayload()
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

    object Rendering {
        enum class Currency {
            KRW, AUD, EUR, GBP, HKD, JPY, SGD, USD
        }

        /**
         * PayPal 해외간편결제 금액 정보
         * @property value : 결제 금액
         * @property currency: 결제 통화
         * @property country : 결제 국가 코드 (https://ko.wikipedia.org/wiki/ISO_3166-1_alpha-2)
         * @since 2023/07/04
         */
        data class Amount(
            val value: Number,
            val currency: Currency = Currency.KRW,
            val country: String = "KR"
        )

        /**
         * 결제위젯의 렌더링 옵션
         * @property variantKey : 멀티 결제 UI를 사용할 때 설정. 렌더링하고 싶은 결제위젯의 키 값
         * @since 2023/07/04
         */
        data class Options(
            val variantKey: String
        )
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
        var useInternationalCardOnly: Boolean? = null

        override val paymentPayload: JSONObject.(JSONObject) -> JSONObject
            get() = {
                remove("amount")

                put("taxExemptionAmount", taxExemptionAmount)
                put("showCustomerMobilePhone", showCustomerMobilePhone)

                useEscrow?.let {
                    put("useEscrow", useEscrow ?: it)
                }

                escrowProducts?.let {
                    val escrowProductsPayload = JSONArray()
                    it.map { escrowProduct -> escrowProduct.json }.forEach { json ->
                        escrowProductsPayload.put(json)
                    }
                    put("escrowProducts", escrowProductsPayload)
                }

                customerMobilePhone?.let {
                    put("customerMobilePhone", it)
                }

                mobileCarrier?.let {
                    put("mobileCarrier", JSONArray().apply {
                        it.forEach { code ->
                            this.put(code)
                        }
                    })
                }

                useInternationalCardOnly?.let {
                    put("useInternationalCardOnly", it)
                }

                this
            }
    }
}