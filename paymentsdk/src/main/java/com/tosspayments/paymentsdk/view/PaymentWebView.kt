package com.tosspayments.paymentsdk.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.tosspayments.paymentsdk.interfaces.PaymentWidgetCallback

@SuppressLint("SetJavaScriptEnabled")
class PaymentWebView(context: Context, attrs: AttributeSet? = null) : WebView(context, attrs) {
    companion object {
        const val JS_INTERFACE_NAME = "PaymentWidgetAndroidSDK"
    }

    init {
        settings.run {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true

            allowUniversalAccessFromFileURLs = true
            allowFileAccessFromFileURLs = true
        }

        webChromeClient = WebChromeClient()
    }

    internal open class PaymentWebViewJavascriptInterface(private val paymentWidgetCallback: PaymentWidgetCallback?) {
        @JavascriptInterface
        fun requestPayments(html: String) {
            paymentWidgetCallback?.onPaymentDomCreated(html)
        }

        @JavascriptInterface
        fun requestHTML(html: String) {
            paymentWidgetCallback?.onHtmlRequested(html)
        }

        @JavascriptInterface
        fun success(html: String) {
            paymentWidgetCallback?.onHtmlRequested(html)
        }
    }

    fun loadLocalHtml(fileName: String) {
        loadUrl("file:///android_asset/$fileName")
    }

    fun loadHtml(html: String) {
        loadData(
            Base64.encodeToString(html.toByteArray(), Base64.NO_PADDING),
            "text/html",
            "base64"
        )
    }
}