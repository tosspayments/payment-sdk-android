package com.tosspayments.paymentsdk.interfaces

internal interface PaymentWidgetCallback {
    fun onPostPaymentHtml(html : String, domain : String? = null)
    fun onHtmlRequested(html : String, domain : String? = null)
    fun onSuccess(response : String, domain : String? = null)
}