package com.tosspayments.paymentsdk.model

open class PaymentMethodEventListener {
    open fun onCustomRequested(paymentMethodKey: String) {}
    open fun onCustomPaymentMethodSelected(paymentMethodKey: String) {}
    open fun onCustomPaymentMethodUnselected(paymentMethodKey: String) {}
}