package com.tosspayments.paymentsdk.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.FrameLayout
import com.tosspayments.paymentsdk.R
import com.tosspayments.paymentsdk.extension.startSchemeIntent
import com.tosspayments.paymentsdk.interfaces.PaymentWidgetJavascriptInterface

sealed class PaymentWidgetContainer(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    protected val paymentWebView: PaymentWebView

    protected var methodRenderCalled = false

    init {
        LayoutInflater.from(context).inflate(R.layout.view_payment_widget, this, true).run {
            paymentWebView = findViewById<PaymentWebView>(R.id.webview_payment).apply {
                this@apply.layoutParams = this@apply.layoutParams.apply {
                    this.height = ViewGroup.LayoutParams.WRAP_CONTENT
                }

                isVerticalScrollBarEnabled = false
            }
        }
    }

    protected fun handleOverrideUrl(requestedUri: Uri?): Boolean {
        return requestedUri?.let { uri ->
            val requestedUrl = uri.toString()

            return when {
                URLUtil.isNetworkUrl(requestedUrl) -> {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    true
                }
                !URLUtil.isJavaScriptUrl(requestedUrl) -> {
                    if ("intent".equals(uri.scheme, true)) {
                        context.startSchemeIntent(requestedUrl)
                    } else {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            true
                        } catch (e: java.lang.Exception) {
                            false
                        }
                    }
                }
                else -> false
            }
        } ?: false
    }

    fun evaluateJavascript(script: String) {
        paymentWebView.evaluateJavascript(script)
    }

    fun addJavascriptInterface(javascriptInterface: PaymentWidgetJavascriptInterface) {
        paymentWebView.addJavascriptInterface(
            javascriptInterface,
            PaymentWebView.INTERFACE_NAME_WIDGET
        )
    }

    internal fun updateHeight(heightPx: Float?) {
        paymentWebView.setHeight(heightPx)
    }
}