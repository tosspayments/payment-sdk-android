package com.tosspayments.paymentsdk.interfaces

internal interface IPaymentWidget {
    fun evaluateJavascript(script: String)
    fun addJavascriptInterface(javascriptInterface: PaymentWebViewJavascriptInterface)
    fun setHeight(heightPx: Float?)
}