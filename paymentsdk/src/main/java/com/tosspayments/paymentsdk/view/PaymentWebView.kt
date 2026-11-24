package com.tosspayments.paymentsdk.view

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import com.tosspayments.paymentsdk.interfaces.BridgeObject
import com.tosspayments.paymentsdk.interfaces.BridgeOrigin
import com.tosspayments.paymentsdk.interfaces.MainThreadReader
import com.tosspayments.paymentsdk.interfaces.PaymentBridge
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

        private const val DEFAULT_BASE_URL = "https://appassets.androidplatform.net"
        private const val MAIN_FRAME_ORIGIN_TIMEOUT_MILLIS = 5000L
    }
    var setHeightPx: Float? = null

    internal var bridgeObjects: List<BridgeObject> = emptyList()

    @Volatile
    private var bridge: PaymentBridge? = null

    private val mainThread = MainThreadReader(MAIN_FRAME_ORIGIN_TIMEOUT_MILLIS) {
        Handler(Looper.getMainLooper()).post(it)
    }

    init {
        settings.run {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
        }

        webChromeClient = WebChromeClient()

        addJavascriptInterface(BridgeEntry(), PaymentBridge.ENTRY_NAME)
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
        val host = baseUrl(domain)
        val htmlFileUrl = "${host}/assets/$htmlFileName"
        val bridge = openBridge(host)

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
                val url = request?.url ?: return super.shouldInterceptRequest(view, request)
                val document = bridge.scriptedDocument(
                    htmlFileUrl,
                    url.toString(),
                    request.isForMainFrame
                ) { readAsset(htmlFileName) }

                return document?.let { htmlResponse(it) }
                    ?: assetLoader.shouldInterceptRequest(url)
                    ?: super.shouldInterceptRequest(view, request)
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
        val baseUrl = baseUrl(domain)

        loadDataWithBaseURL(
            baseUrl,
            openBridge(baseUrl).addScript(html),
            "text/html; charset=utf-8",
            "utf-8",
            baseUrl
        )
    }

    internal fun loadAsset(htmlFileName: String) {
        loadHtml(readAsset(htmlFileName))
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

    private inner class BridgeEntry {
        @JavascriptInterface
        fun invoke(token: String?, target: String?, method: String?, arguments: String?) {
            bridge?.receive(token, target, method, arguments) {
                mainThread.read { Uri.parse(url.orEmpty()).toBridgeOrigin() }
            }
        }
    }

    private fun baseUrl(domain: String?): String {
        return if (domain.isNullOrBlank()) DEFAULT_BASE_URL else "https://$domain"
    }

    private fun openBridge(baseUrl: String): PaymentBridge {
        return PaymentBridge.open(Uri.parse(baseUrl).toBridgeOrigin(), bridgeObjects)
            .also { bridge = it }
    }

    private fun readAsset(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    private fun htmlResponse(html: String): WebResourceResponse {
        return WebResourceResponse("text/html", "utf-8", html.byteInputStream())
    }

    private fun Uri.toBridgeOrigin(): BridgeOrigin {
        return BridgeOrigin(scheme, host, port)
    }
}
