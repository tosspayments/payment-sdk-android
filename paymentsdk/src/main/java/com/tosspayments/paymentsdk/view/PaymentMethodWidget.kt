package com.tosspayments.paymentsdk.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import androidx.core.widget.ContentLoadingProgressBar
import com.tosspayments.paymentsdk.R
import com.tosspayments.paymentsdk.extension.startSchemeIntent
import com.tosspayments.paymentsdk.interfaces.PaymentWidgetCallback
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@SuppressLint("SetJavaScriptEnabled")
class PaymentMethodWidget(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    private val webViewContainer: ViewGroup
    private val loadingProgressBar: ContentLoadingProgressBar
    private val paymentWebView: WebView

    private var methodRenderCalled: Boolean = false
    private var paymentWidgetCallback: PaymentWidgetCallback? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_tosspayment, this, true).run {
            webViewContainer = findViewById<ViewGroup>(R.id.payment_webview_container).apply {
                this@apply.layoutParams = this@apply.layoutParams.apply {
                    this.height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
            }

            loadingProgressBar = findViewById(R.id.progress_loading)

            paymentWebView = findViewById<WebView?>(R.id.webview_payment).apply {
                this@apply.layoutParams = this@apply.layoutParams.apply {
                    this.height = ViewGroup.LayoutParams.WRAP_CONTENT
                }

                settings.javaScriptEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true

                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false

                webChromeClient = WebChromeClient()

                addJavascriptInterface(
                    TossPaymentWidgetJavascriptInterface(),
                    "PaymentWidgetAndroidSDK"
                )
            }
        }
    }

    private inner class TossPaymentWidgetJavascriptInterface {
        @JavascriptInterface
        fun requestPayments(paymentDom: String) {
            paymentWidgetCallback?.onPaymentDomCreated(paymentDom)
        }

        @JavascriptInterface
        fun updateHeight(height: String?) {
            height?.toFloat()?.let {
                setHeight(it)
            }
        }
    }

    private fun getPaymentWebViewClient(onPageFinished: WebView.() -> Unit): WebViewClient {
        return object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                loadingProgressBar.visibility = View.GONE
                paymentWebView.visibility = View.VISIBLE

                view?.onPageFinished()
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

    internal fun renderPaymentMethods(clientKey: String, customerKey: String, amount: Long) {
        val renderMethodScript = StringBuilder()
            .appendLine("var paymentWidget = PaymentWidget('$clientKey', '$customerKey');")
            .appendLine("paymentWidget.renderPaymentMethods('#payment-method', $amount);")
            .toString()

        paymentWebView.run {
            webViewClient = getPaymentWebViewClient {
                this.evaluateJavascript("javascript:$renderMethodScript", null)
                methodRenderCalled = true
            }

            loadUrl("file:///android_asset/tosspayment_widget.html")
        }
    }

    @JvmOverloads
    @Throws(IllegalAccessException::class)
    internal fun requestPayment(
        orderId: String,
        orderName: String,
        customerEmail: String? = null,
        customerName: String? = null,
        paymentWidgetCallback: PaymentWidgetCallback
    ) {
        if (methodRenderCalled) {
            this.paymentWidgetCallback = paymentWidgetCallback

            val requestPaymentScript = "paymentWidget.requestPaymentForNativeSDK({\n" +
                    "orderId: '${orderId}',\n" +
                    "orderName: '${orderName}',\n" +
                    "successUrl: '${TossPaymentInfo.successUri}',\n" +
                    "failUrl: '${TossPaymentInfo.failUri}',\n" +
                    "customerEmail: '${customerEmail.orEmpty()}'," +
                    "customerName: '${customerName.orEmpty()}'" +
                    "})"

            paymentWebView.evaluateJavascript(requestPaymentScript, null)
        } else {
            this.paymentWidgetCallback = null
            throw IllegalArgumentException("renderPaymentMethods method should be called before the payment requested.")
        }
    }

    private fun setHeight(height: Float) {
        runBlocking {
            val convertedHeight = withContext(Dispatchers.Default) {
                (height * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
            }

            launch(Dispatchers.Main) {
                paymentWebView.layoutParams = paymentWebView.layoutParams.apply {
                    this.height = convertedHeight
                }
            }
        }
    }
}