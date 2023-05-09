package com.tosspayments.paymentsdk.sample

import android.app.Application
import android.webkit.WebView

class PaymentSdkSampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        WebView.setWebContentsDebuggingEnabled(true)
    }
}