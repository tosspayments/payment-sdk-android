package com.tosspayments.paymentsdk.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.widget.FrameLayout
import com.tosspayments.paymentsdk.R
import com.tosspayments.paymentsdk.extension.startSchemeIntent
import com.tosspayments.paymentsdk.interfaces.PaymentWidgetCallback
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
class PaymentMethodWidget(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    private val paymentWebView: PaymentWebView

    private var methodRenderCalled: Boolean = false

    private val defaultScope = CoroutineScope(Job() + Dispatchers.Default)

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

    private inner class TossPaymentWidgetJavascriptInterface(
        domain: String?,
        paymentWidgetCallback: PaymentWidgetCallback?
    ) : PaymentWebView.PaymentWebViewJavascriptInterface(paymentWidgetCallback, domain) {
        @JavascriptInterface
        fun updateHeight(height: String?) {
            height?.toFloat()?.let {
                setHeight(it)
            }
        }

        @JavascriptInterface
        fun getAppInfo(os: String, appId: String, sdkVersion: String) {
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

    private val redirectOption: (redirectUrl: String?) -> String? = {
        it?.let { redirectUrl -> "{'brandpay':{'redirectUrl':'$redirectUrl'}}" }
    }

    internal fun renderPaymentMethods(
        clientKey: String,
        customerKey: String,
        amount: Number,
        redirectUrl: String? = null,
        paymentWidgetCallback: PaymentWidgetCallback? = null
    ) {
        val paymentWidgetConstructor =
            "PaymentWidget('$clientKey', '$customerKey', ${redirectOption(redirectUrl)})"

        val renderMethodScript = StringBuilder()
            .appendLine("var paymentWidget = $paymentWidgetConstructor;")
            .appendLine("paymentWidget.renderPaymentMethods('#payment-method', $amount);")
            .toString()

        val domain = try {
            Uri.parse(redirectUrl).host
        } catch (e: Exception) {
            null
        }

        paymentWebView.loadHtml(
            domain,
            "tosspayment_widget.html",
            TossPaymentWidgetJavascriptInterface(domain, paymentWidgetCallback),
            {
                evaluateJavascript(renderMethodScript)
                methodRenderCalled = true
            },
            {
                handleOverrideUrl(this)
            }
        )
    }

    @JvmOverloads
    @Throws(IllegalAccessException::class)
    internal fun requestPayment(
        orderId: String,
        orderName: String,
        customerEmail: String? = null,
        customerName: String? = null,
        redirectUrl: String? = null
    ) {
        if (methodRenderCalled) {
            val requestPaymentScript = StringBuilder().apply {
                appendLine("paymentWidget.requestPaymentForNativeSDK({")
                appendLine("orderId: '${orderId}',")
                appendLine("orderName: '${orderName}',")
                appendLine("successUrl: '${TossPaymentInfo.successUri}',")
                appendLine("failUrl: '${TossPaymentInfo.failUri}',")
                appendLine("customerEmail: '${customerEmail.orEmpty()}',")
                appendLine("customerName: '${customerName.orEmpty()}',")
                appendLine("redirectUrl : ${redirectOption(redirectUrl)}})")
            }.toString()

            paymentWebView.evaluateJavascript(requestPaymentScript, null)
        } else {
            throw IllegalArgumentException("renderPaymentMethods method should be called before the payment requested.")
        }
    }

    internal fun evaluateJavascript(script: String) {
        paymentWebView.evaluateJavascript("javascript:$script", null)
    }

    private fun setHeight(heightPx: Float) {
        defaultScope.launch {
            val convertedHeight =
                (heightPx * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

            launch(Dispatchers.Main) {
                paymentWebView.layoutParams = paymentWebView.layoutParams.apply {
                    this.height = convertedHeight
                }
            }
        }
    }
}