package com.tosspayments.paymentsdk.interfaces

import android.webkit.JavascriptInterface

interface PaymentJavascriptInterface

abstract class PaymentWidgetJavascriptInterface : PaymentJavascriptInterface {
    @JavascriptInterface
    abstract fun message(json: String)
}

