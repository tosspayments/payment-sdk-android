package com.tosspayments.paymentsdk.view

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
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
import com.tosspayments.paymentsdk.interfaces.TossPaymentCallback
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentMethod
import org.json.JSONObject
import java.net.URISyntaxException

@SuppressLint("SetJavaScriptEnabled")
class TossPaymentView(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    private val successUri = TossPaymentInfo.successUri
    private val failUri = TossPaymentInfo.failUri

    private val loadingProgressBar: ContentLoadingProgressBar
    private val paymentWebView: WebView

    var callback: TossPaymentCallback? = null

    companion object {
        const val CONST_PAYMENT_KEY = "paymentKey"
        const val CONST_ORDER_ID = "orderId"
        const val CONST_AMOUNT = "amount"
        const val CONST_CODE = "code"
        const val CONST_MESSAGE = "message"
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

            val isSuccess =
                successUri.scheme.equals(uri.scheme) && successUri.host.equals(uri.host) && successUri.path.equals(
                    uri.path
                )

            val isCanceled = "PAY_PROCESS_CANCELED".equals(uri.getQueryParameter("code"), true)

            val isFailed =
                failUri.scheme.equals(uri.scheme) && failUri.host.equals(uri.host) && failUri.path.equals(
                    uri.path
                )

            if (isSuccess) {
                callback?.run {
                    onSuccess(
                        TossPaymentResult.Success(
                            paymentKey = uri.getQueryParameter(CONST_PAYMENT_KEY)
                                .orEmpty(),
                            orderId = uri.getQueryParameter(CONST_ORDER_ID).orEmpty(),
                            amount = uri.getQueryParameter(CONST_AMOUNT)?.toLong() ?: 0
                        )
                    )
                    true
                } ?: false
            } else if (isFailed || isCanceled) {
                callback?.run {
                    onFailed(
                        TossPaymentResult.Fail(
                            errorCode = uri.getQueryParameter(CONST_CODE).orEmpty(),
                            errorMessage = uri.getQueryParameter(CONST_MESSAGE).orEmpty(),
                            orderId = uri.getQueryParameter(CONST_ORDER_ID).orEmpty()
                        )
                    )
                    true
                } ?: false
            } else if (!URLUtil.isNetworkUrl(requestedUrl)
                && !URLUtil.isJavaScriptUrl(requestedUrl)
            ) {
                return when (uri.scheme) {
                    "intent" -> {
                        startSchemeIntent(requestedUrl)
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

    private fun startSchemeIntent(url: String): Boolean {
        val schemeIntent: Intent = try {
            Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
        } catch (e: URISyntaxException) {
            return false
        }

        try {
            context.startActivity(schemeIntent)
            return true
        } catch (e: ActivityNotFoundException) {
            val packageName = schemeIntent.getPackage()

            if (!packageName.isNullOrBlank()) {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    )
                )
                return true
            }
        }
        return false
    }

    internal inner class TossPaymentJavascriptInterface {
        @JavascriptInterface
        fun onError(errorCode: String, message: String, orderId: String) {
            callback?.onFailed(
                TossPaymentResult.Fail(
                    errorCode = errorCode,
                    errorMessage = message,
                    orderId = orderId
                )
            )
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_tosspayment, this, true).run {
            loadingProgressBar = findViewById(R.id.progress_loading)

            paymentWebView = findViewById<WebView?>(R.id.webview_payment).apply {
                settings.javaScriptEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                webChromeClient = WebChromeClient()

                addJavascriptInterface(TossPaymentJavascriptInterface(), "TossPayment")
            }
        }
    }

    fun requestPayment(clientKey: String, method: TossPaymentMethod, paymentInfo: TossPaymentInfo) {
        requestPayment(clientKey, method.displayName, paymentInfo.getPayload().toString())
    }

    internal fun requestPayment(clientKey: String, methodName: String, jsonPayload: String) {
        val orderId =
            kotlin.runCatching { JSONObject(jsonPayload).get("orderId").toString() }.getOrNull()
                .orEmpty()

        val requestPaymentScript = StringBuilder()
            .append("var tossPayments = TossPayments('${clientKey}');")
            .append("tossPayments.requestPayment(")
            .append("'${methodName}', ${jsonPayload})")
            .append(".catch(function (error) {")
            .append("TossPayment.onError(error.code, error.message,'${orderId}');")
            .append("})")
            .toString()

        requestPayment(requestPaymentScript)
    }

    private fun requestPayment(paymentInfoPayload: String) {
        showLoading(true)

        paymentWebView.webViewClient = getPaymentWebViewClient {
            paymentWebView.evaluateJavascript("javascript:$paymentInfoPayload", null)

            showLoading(false)
        }

        paymentWebView.loadUrl("file:///android_asset/tosspayment.html")
    }

    private fun showLoading(isShown: Boolean) {
        loadingProgressBar.visibility = if (isShown) View.VISIBLE else View.GONE
        paymentWebView.visibility = if (isShown) View.GONE else View.VISIBLE
    }
}