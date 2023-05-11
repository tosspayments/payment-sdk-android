package com.tosspayments.paymentsdk.view

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Base64
import android.util.DisplayMetrics
import android.webkit.*
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import com.tosspayments.paymentsdk.interfaces.PaymentWebViewJavascriptInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
class PaymentWebView(context: Context, attrs: AttributeSet? = null) : WebView(context, attrs) {
    private val defaultScope = CoroutineScope(Job() + Dispatchers.Default)

    companion object {
        const val JS_INTERFACE_NAME = "PaymentWidgetAndroidSDK"
    }

    init {
        settings.run {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
        }

        webChromeClient = WebChromeClient()
    }

//    internal open class PaymentWebViewJavascriptInterface(
//        private val paymentWidgetCallback: PaymentWidgetCallback? = null,
//        private val domain: String? = null
//    ) {
//        @JavascriptInterface
//        fun requestPayments(html: String) {
//            paymentWidgetCallback?.onPostPaymentHtml(html, domain)
//        }
//
//        @JavascriptInterface
//        fun requestHTML(html: String) {
//            paymentWidgetCallback?.onHtmlRequested(html, domain)
//        }
//
//        @JavascriptInterface
//        fun success(html: String) {
//            paymentWidgetCallback?.onSuccess(html, domain)
//        }
//    }

    @SuppressLint("JavascriptInterface")
    internal fun addJavascriptInterface(javascriptInterface: PaymentWebViewJavascriptInterface) {
        addJavascriptInterface(javascriptInterface, JS_INTERFACE_NAME)
    }

    internal fun loadHtml(
        domain: String?,
        htmlFileName: String,
        onPageFinished: WebView.() -> Unit,
        shouldOverrideUrlLoading: Uri?.() -> Boolean
    ) {
        val htmlFileUrl =
            "https://${domain ?: "appassets.androidplatform.net"}/assets/$htmlFileName"

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(context))
            .apply {
                domain?.let {
                    setDomain(it)
                }
            }
            .build()

        webViewClient = object : WebViewClientCompat() {
            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                view.onPageFinished()
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                return request?.url?.let {
                    assetLoader.shouldInterceptRequest(it)
                } ?: super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                return shouldOverrideUrlLoading(request.url)
            }
        }

        loadUrl(htmlFileUrl)
    }

    fun loadHtml(html: String, domain: String? = null) {
        domain?.let {
            val baseUrl = "https://$it"

            loadDataWithBaseURL(
                baseUrl,
                html,
                "text/html; charset=utf-8",
                "utf-8",
                baseUrl
            )
        } ?: kotlin.run {
            loadData(
                Base64.encodeToString(html.toByteArray(), Base64.NO_PADDING),
                "text/html",
                "base64"
            )
        }
    }

    internal fun evaluateJavascript(script: String) {
        evaluateJavascript("javascript:$script", null)
    }

    internal fun setHeight(heightPx: Float?) {
        heightPx?.let {
            defaultScope.launch {
                val convertedHeight =
                    (heightPx * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

                launch(Dispatchers.Main) {
                    layoutParams = layoutParams.apply {
                        this.height = convertedHeight
                    }
                }
            }
        }
    }
}