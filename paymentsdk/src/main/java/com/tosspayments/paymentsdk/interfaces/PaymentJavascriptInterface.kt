package com.tosspayments.paymentsdk.interfaces

import android.webkit.JavascriptInterface
import com.tosspayments.paymentsdk.view.PaymentWidgetContainer

interface PaymentJavascriptInterface

open class PaymentWidgetJavascriptInterface(private val paymentWidgetContainer: PaymentWidgetContainer) :
    PaymentJavascriptInterface {
    @JavascriptInterface
    fun updateHeight(height: String?) {
        paymentWidgetContainer.setHeight(height?.toFloat())
    }
}