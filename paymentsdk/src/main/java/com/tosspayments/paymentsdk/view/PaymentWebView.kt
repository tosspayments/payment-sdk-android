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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
class PaymentWebView(context: Context, attrs: AttributeSet? = null) : WebView(context, attrs) {
    private val defaultScope = CoroutineScope(Job() + Dispatchers.Default)

    companion object {
        const val INTERFACE_NAME_PAYMENT = "TossPayment"
    }

    init {
        settings.run {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
        }

        webChromeClient = WebChromeClient()
    }

    internal fun loadHtml(
        domain: String?,
        htmlFileName: String,
        onPageFinished: WebView.() -> Unit,
        shouldOverrideUrlLoading: Uri?.() -> Boolean
    ) {
        val host = if (domain.isNullOrBlank()) {
            "https://appassets.androidplatform.net"
        } else {
            "https://$domain"
        }

        val htmlFileUrl = "${host}/assets/$htmlFileName"

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(context))
            .apply {
                if (!domain.isNullOrBlank()) {
                    setDomain(domain)
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
        if (domain.isNullOrBlank()) {
            loadData(
                Base64.encodeToString(html.toByteArray(), Base64.NO_PADDING),
                "text/html",
                "base64"
            )
        } else {
            val baseUrl = "https://$domain"

            loadDataWithBaseURL(
                baseUrl,
                html,
                "text/html; charset=utf-8",
                "utf-8",
                baseUrl
            )
        }
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