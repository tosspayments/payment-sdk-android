package com.tosspayments.paymentsdk.interfaces

internal interface PaymentWidgetCallback {
    fun onPaymentDomCreated(html : String)
    fun onHtmlRequested(html : String)
    fun onHtmlRequestSucceeded(html : String)
}