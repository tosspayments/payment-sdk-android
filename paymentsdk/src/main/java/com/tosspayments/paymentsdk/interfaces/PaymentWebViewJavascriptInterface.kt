package com.tosspayments.paymentsdk.interfaces

import android.webkit.JavascriptInterface

interface PaymentWebViewJavascriptInterface {
    @JavascriptInterface
    fun message(message: Any)
}