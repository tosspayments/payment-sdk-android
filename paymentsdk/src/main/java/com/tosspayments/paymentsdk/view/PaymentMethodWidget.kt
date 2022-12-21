package com.tosspayments.paymentsdk.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.core.widget.ContentLoadingProgressBar
import com.tosspayments.paymentsdk.R
import com.tosspayments.paymentsdk.TossPayments
import com.tosspayments.paymentsdk.extension.startSchemeIntent
import com.tosspayments.paymentsdk.extension.toDp
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo

@SuppressLint("SetJavaScriptEnabled")
class PaymentMethodWidget(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    private val webViewContainer: ViewGroup
    private val loadingProgressBar: ContentLoadingProgressBar
    private val paymentWebView: WebView

    private var methodRenderCalled: Boolean = false
    private var requestCode: Int? = null
    private var paymentResultLauncher: ActivityResultLauncher<Intent>? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_tosspayment, this, true).run {
            webViewContainer = findViewById(R.id.payment_webview_container)

            loadingProgressBar = findViewById(R.id.progress_loading)

            paymentWebView = findViewById<WebView?>(R.id.webview_payment).apply {
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

            setHeight(1200)
        }
    }

    private var tossPayments: TossPayments? = null

    internal inner class TossPaymentWidgetJavascriptInterface {
        @JavascriptInterface
        fun requestPayments(payload: String) {
            if (payload.isNotBlank()) {
                when {
                    requestCode != null -> {
                        tossPayments?.requestPayment(
                            context,
                            payload,
                            requestCode!!
                        )
                    }
                    paymentResultLauncher != null -> {
                        tossPayments?.requestPayment(
                            context,
                            payload,
                            paymentResultLauncher!!
                        )
                    }
                }
            }
        }
    }

    private fun getPaymentWebViewClient(onPageFinished: WebView.() -> Unit): WebViewClient {
        return object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                paymentWebView.visibility = View.VISIBLE
                loadingProgressBar.visibility = View.GONE

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

    fun renderPaymentMethods(clientKey: String, customerKey: String, amount: Long) {
        tossPayments = TossPayments(clientKey)

        val renderMethodScript = StringBuilder()
            .appendLine("var paymentWidget = PaymentWidget('$clientKey', '$customerKey');")
            .appendLine("paymentWidget.renderPaymentMethods('#payment-method', $amount);")
            .toString()

        paymentWebView.webViewClient = getPaymentWebViewClient {
            evaluateJavascript("javascript:$renderMethodScript", null)
            methodRenderCalled = true
        }

        paymentWebView.loadUrl("file:///android_asset/tosspayment_widget.html")
    }

    @JvmOverloads
    @Throws(IllegalAccessException::class)
    fun requestPayment(
        paymentResultLauncher: ActivityResultLauncher<Intent>,
        orderId: String,
        orderName: String,
        customerEmail: String? = null,
        customerName: String? = null,
    ) {
        this.paymentResultLauncher = paymentResultLauncher
        requestPayment(orderId, orderName, customerEmail, customerName)
    }

    @JvmOverloads
    @Throws(IllegalAccessException::class)
    fun requestPayment(
        requestCode: Int,
        orderId: String,
        orderName: String,
        customerEmail: String? = null,
        customerName: String? = null,
    ) {
        this.requestCode = requestCode
        requestPayment(orderId, orderName, customerEmail, customerName)
    }

    @JvmOverloads
    @Throws(IllegalAccessException::class)
    private fun requestPayment(
        orderId: String,
        orderName: String,
        customerEmail: String? = null,
        customerName: String? = null,
    ) {
        if (methodRenderCalled) {
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
            throw IllegalArgumentException("renderPaymentMethods method should be called before the payment requested.")
        }
    }

    private fun setHeight(height: Float) {
        setHeight(height.toDp(context))
    }

    private fun setHeight(height: Int) {
        webViewContainer.layoutParams = webViewContainer.layoutParams.apply {
            this.height = height
        }
    }
}