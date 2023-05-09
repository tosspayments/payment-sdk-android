package com.tosspayments.paymentsdk.model

class PaymentWidgetOptions private constructor(
    internal val brandPayOption: BrandPayOption? = null
) {
    internal class BrandPayOption(val redirectUrl: String)

    class Builder {
        private var brandPayOption: BrandPayOption? = null

        fun brandPayOption(redirectUrl: String): Builder {
            brandPayOption = BrandPayOption(redirectUrl)
            return this
        }

        fun build(): PaymentWidgetOptions {
            return PaymentWidgetOptions(brandPayOption)
        }
    }
}
