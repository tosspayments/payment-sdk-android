package com.tosspayments.paymentsdk.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import androidx.core.widget.ContentLoadingProgressBar
import com.tosspayments.paymentsdk.R
import com.tosspayments.paymentsdk.extension.startSchemeIntent

@SuppressLint("SetJavaScriptEnabled")
class PaymentWidget(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    private val loadingProgressBar: ContentLoadingProgressBar
    private val paymentWebView: WebView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_tosspayment, this, true).run {
            loadingProgressBar = findViewById(R.id.progress_loading)

            paymentWebView = findViewById<WebView?>(R.id.webview_payment).apply {
                settings.javaScriptEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                webChromeClient = WebChromeClient()

//                addJavascriptInterface(TossPaymentJavascriptInterface(), "TossPayment")
            }

            paymentWebView.loadUrl("file:///android_asset/tosspayment_widget.html")
        }
    }

    private fun getPaymentWebViewClient(onPageFinished: () -> Unit): WebViewClient {
        return object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onPageFinished.invoke()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return handleOverrideUrl(request?.url)
            }
        }
    }

    private fun handleOverrideUrl(requestedUri: Uri?): Boolean {
        return requestedUri?.let { uri ->
            val requestedUrl = uri.toString()

            if (!URLUtil.isNetworkUrl(requestedUrl)
                && !URLUtil.isJavaScriptUrl(requestedUrl)
            ) {
                return when (uri.scheme) {
                    "intent" -> {
                        context.startSchemeIntent(requestedUrl)
                    }
                    else -> {
                        return try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            true
                        } catch (e: java.lang.Exception) {
                            false
                        }
                    }
                }
            } else {
                return false
            }
        } ?: false
    }

    fun renderPaymentMethods(clientKey: String, customerKey: String, amount: Long) {
        val renderMethodScript = StringBuilder()
            .appendLine("var paymentWidget = PaymentWidget('$clientKey', '$customerKey');")
            .appendLine("paymentWidget.renderPaymentMethods('#payment-method', $amount);")
            .toString()

        paymentWebView.webViewClient = getPaymentWebViewClient {
            paymentWebView.run {
                evaluateJavascript("javascript:$renderMethodScript", null)
                visibility = View.VISIBLE
            }

            loadingProgressBar.visibility = View.GONE
        }

        paymentWebView.loadUrl("file:///android_asset/tosspayment_widget.html")
    }
}