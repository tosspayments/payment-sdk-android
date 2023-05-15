package com.tosspayments.paymentsdk.interfaces

import android.webkit.JavascriptInterface
import com.tosspayments.paymentsdk.view.PaymentWebView

open class PaymentWebViewJavascriptInterface(private val paymentWebView: PaymentWebView) {
    @JavascriptInterface
    fun updateHeight(height: String?) {
        paymentWebView.setHeight(height?.toFloat())
    }
}