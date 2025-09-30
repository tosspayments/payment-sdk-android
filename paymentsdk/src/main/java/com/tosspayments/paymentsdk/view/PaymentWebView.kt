package com.tosspayments.paymentsdk.view

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Base64
import android.util.DisplayMetrics
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("SetJavaScriptEnabled")
class PaymentWebView(context: Context, attrs: AttributeSet? = null) : WebView(context, attrs) {
    private val defaultScope = CoroutineScope(Job() + Dispatchers.Default)

    companion object {
        const val INTERFACE_NAME_PAYMENT = "TossPayment"
    }
    var setHeightPx: Float? = null

    init {
        settings.run {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
        }

        webChromeClient = WebChromeClient()
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        // NOTE(@JooYang): ScrollView 내에서 결제위젯 웹뷰를 사용할 시 간혹 웹뷰 내에 비정상적인 스크롤 영역이 생긴다.
        // 이로 인해 결제위젯 내 요소(특히 select)를 클릭할 때 스크롤이 되어 웹뷰 UI가 가려지게 되는데, 이 현상을 근본적으로 막기는 어렵다.
        // 대신에, 이 현상이 발생했을 때 높이를 약간 다르게 re-render 시키면 스크롤 영역 없이 정상 렌더링이 되기 때문에 이를 통해 해결한다.
        if (scrollY > 0) {
            setHeightPx?.let {
                this.setHeight(it.roundToInt().toFloat() + 1f)
            }
        }
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
        setHeightPx = heightPx
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