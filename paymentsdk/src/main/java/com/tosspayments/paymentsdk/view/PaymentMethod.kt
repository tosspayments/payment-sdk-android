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

    class PaymentInfo private constructor(
        builder: Builder
    ) : TossPaymentInfo(builder.orderId, builder.orderName, 0) {
        val orderId: String
        val orderName: String
        var taxExemptionAmount: Number = 0
        var useEscrow: Boolean? = null
        var escrowProducts: List<EscrowProduct>? = null
        var customerMobilePhone: String? = null
        var showCustomerMobilePhone: Boolean = false
        var mobileCarrier: List<TossPaymentMobileCarrier>? = null
        var useInternationalCardOnly: Boolean? = null
        var metadata: Map<String, String>? = null

        @Deprecated(
            "해당 방식은 삭제할 예정입니다. Builder 사용해주세요.",
            ReplaceWith("PaymentMethod.PaymentInfo.Builder()")
        )
        constructor(
            orderId: String,
            orderName: String
        ) : this(
            Builder()
                .setOrderId(orderId)
                .setOrderName(orderName)
        )

        init {
            orderId = builder.orderId
            orderName = builder.orderName
            taxExemptionAmount = builder.taxExemptionAmount
            useEscrow = builder.useEscrow
            escrowProducts = builder.escrowProducts
            customerMobilePhone = builder.customerMobilePhone
            showCustomerMobilePhone = builder.showCustomerMobilePhone
            mobileCarrier = builder.mobileCarrier

            customerName = builder.customerName
            customerEmail = builder.customerEmail
            taxFreeAmount = builder.taxFreeAmount
            cultureExpense = builder.cultureExpense
            useInternationalCardOnly = builder.useInternationalCardOnly
        }

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

                metadata?.let {
                    put("metadata", JSONObject(it))
                }

                this
            }

        @Suppress("unused", "MemberVisibilityCanBePrivate")
        class Builder {
            internal var orderId: String = ""
                private set
            internal var orderName: String = ""
                private set
            internal var amount: Long = 0
                private set
            internal var taxExemptionAmount: Number = 0
                private set
            internal var useEscrow: Boolean? = null
                private set
            internal var escrowProducts: List<EscrowProduct>? = null
                private set
            internal var customerMobilePhone: String? = null
                private set
            internal var showCustomerMobilePhone: Boolean = false
                private set
            internal var mobileCarrier: List<TossPaymentMobileCarrier>? = null
                private set

            internal var customerName: String? = null
                private set
            internal var customerEmail: String? = null
                private set
            internal var taxFreeAmount: Number? = null
                private set
            internal var cultureExpense: Boolean = false
                private set
            internal var useInternationalCardOnly: Boolean? = null
                private set

            /**
             * Setter
             * @param id 주문 번호
             */
            fun setOrderId(id: Int): Builder {
                return setOrderId("$id")
            }

            /**
             * Setter
             * @param id 주문 번호
             */
            fun setOrderId(id: Long): Builder {
                return setOrderId("$id")
            }

            /**
             * Setter
             * @param id 주문 번호
             */
            fun setOrderId(id: String): Builder {
                this.orderId = id
                return this
            }

            /**
             * Setter
             * @param name 주문명
             */
            fun setOrderName(name: String): Builder {
                this.orderName = name
                return this
            }

            /**
             * Setter
             * @param amount 주문 금액
             */
            fun setAmount(amount: Int): Builder {
                return setAmount(amount.toLong())
            }

            /**
             * Setter
             * @param amount 주문 금액
             */
            fun setAmount(amount: Long): Builder {
                this.amount = amount
                return this
            }

            /**
             * Setter
             * @param amount 세금 면제 금액
             */
            fun setTaxExemptionAmount(amount: Number): Builder {
                this.taxExemptionAmount = amount
                return this
            }

            /**
             * Setter
             * @param useEscrow 에스크로 처리 유무
             */
            fun setUseEscrow(useEscrow: Boolean?): Builder {
                this.useEscrow = useEscrow
                return this
            }

            /**
             * Setter
             * @param list 에스크로 상품
             */
            fun setEscroProducts(list: List<EscrowProduct>?): Builder {
                if (list == null) {
                    this.escrowProducts = null
                    return this
                }
                this.escrowProducts = list.map { it.copy() }
                return this
            }

            /**
             * Setter
             * @param phone 구매자 휴대폰 번호
             */
            fun setCustomerMobilePhone(phone: String?): Builder {
                this.customerMobilePhone = phone
                return this
            }

            /**
             * Setter
             * @param isShowMobilePhone 휴대폰 번호 노출 유무
             */
            fun setShowCustomerMobilePhone(isShowMobilePhone: Boolean): Builder {
                this.showCustomerMobilePhone = isShowMobilePhone
                return this
            }

            /**
             * Setter
             * @param list 휴대폰 결제창에 보여줄 통신사
             */
            fun setMobileCarrier(list: List<TossPaymentMobileCarrier>?): Builder {
                this.mobileCarrier = list
                return this
            }

            /**
             * Setter
             * @param name 구매자 명
             */
            fun setCustomerName(name: String?): Builder {
                this.customerName = name
                return this
            }

            /**
             * Setter
             * @param email 구매자 메일
             */
            fun setCustomerEmail(email: String?): Builder {
                this.customerEmail = email
                return this
            }

            /**
             * Setter
             * @param amount 면세 금액
             */
            fun setTaxFreeAmount(amount: Number?): Builder {
                this.taxFreeAmount = amount
                return this
            }

            fun setCultureExpense(isCultureExpense: Boolean): Builder {
                this.cultureExpense = isCultureExpense
                return this
            }

            fun setUseCardOnly(useCardOnly: Boolean): Builder {
                this.useInternationalCardOnly = useCardOnly
                return this
            }

            /**
             * 변수 유효성 검사 하는 함수
             */
            private fun validationCheck() {
                if (orderId.isEmpty() || orderName.isEmpty()) {
                    throw IllegalArgumentException("주문번호와 주문명은 필수값입니다. orderId=${orderId} orderName=${orderName}")
                }
            }

            fun build(): PaymentInfo {
                validationCheck()
                return PaymentInfo(this)
            }
        }
    }
}