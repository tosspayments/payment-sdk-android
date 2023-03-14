package com.tosspayments.paymentsdk.interfaces

internal interface PaymentWidgetCallback {
    fun onPostPaymentHtml(html : String)
    fun onHtmlRequested(domain : String?, html : String)
    fun onHtmlRequestSucceeded(html : String)
}