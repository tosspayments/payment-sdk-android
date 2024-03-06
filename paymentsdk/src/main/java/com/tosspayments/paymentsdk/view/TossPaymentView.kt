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
import com.tosspayments.paymentsdk.R
import com.tosspayments.paymentsdk.extension.startSchemeIntent
import com.tosspayments.paymentsdk.interfaces.PaymentJavascriptInterface
import com.tosspayments.paymentsdk.interfaces.TossPaymentCallback
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentMethod
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
class TossPaymentView(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    private val successUri = TossPaymentInfo.successUri
    private val failUri = TossPaymentInfo.failUri

    private val paymentWebView: PaymentWebView

    var callback: TossPaymentCallback? = null

    companion object {
        const val CONST_PAYMENT_KEY = "paymentKey"
        const val CONST_ORDER_ID = "orderId"
        const val CONST_AMOUNT = "amount"
        const val CONST_CODE = "code"
        const val CONST_MESSAGE = "message"
    }

    private fun getPaymentWebViewClient(onPageFinished: (WebView.() -> Unit)? = null): WebViewClient {
        return object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                showLoading(false)

                if (onPageFinished != null) {
                    view?.onPageFinished()
                }
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

            val isCanceled = try {
                "PAY_PROCESS_CANCELED".equals(uri.getQueryParameter("code"), true)
            } catch (e: Exception) {
                false
            }

            val isFailed =
                failUri.scheme.equals(uri.scheme) && failUri.host.equals(uri.host) && failUri.path.equals(
                    uri.path
                )

            if (isSuccess) {
                callback?.run {
                    val additionalParameters = uri.queryParameterNames.filter { key ->
                        key !in arrayOf(CONST_PAYMENT_KEY, CONST_ORDER_ID, CONST_AMOUNT)
                    }.associateWith { key -> uri.getQueryParameter(key).orEmpty() }

                    onSuccess(
                        TossPaymentResult.Success(
                            paymentKey = uri.getQueryParameter(CONST_PAYMENT_KEY).orEmpty(),
                            orderId = uri.getQueryParameter(CONST_ORDER_ID).orEmpty(),
                            amount = uri.getQueryParameter(CONST_AMOUNT)?.toDouble() ?: 0,
                            additionalParameters = additionalParameters
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
            } else if (!URLUtil.isNetworkUrl(requestedUrl) && !URLUtil.isJavaScriptUrl(requestedUrl)) {
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
                /*
                 * 삼성카드 백신 앱 onestore 링크 대응
                 */
                if (requestedUrl.startsWith("https://m.onestore") || requestedUrl.startsWith("https://onesto.re")) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    return true
                }
                return false
            }
        } ?: false
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_tosspayment, this, true).run {
            paymentWebView = findViewById<PaymentWebView>(R.id.webview_payment).apply {
                addJavascriptInterface(object : PaymentJavascriptInterface {
                    @JavascriptInterface
                    fun error(errorCode: String, message: String, orderId: String?) {
                        callback?.onFailed(
                            TossPaymentResult.Fail(
                                errorCode = errorCode,
                                errorMessage = message,
                                orderId = orderId
                            )
                        )
                    }
                }, PaymentWebView.INTERFACE_NAME_PAYMENT)

                loadUrl("file:///android_asset/tosspayment.html")
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
            .append("TossPayment.error(error.code, error.message,'${orderId}');")
            .append("})")
            .toString()

        requestPayment(requestPaymentScript)
    }

    internal fun requestPaymentHtml(html: String, domain: String? = null) {
        showLoading(true)

        paymentWebView.run {
            webViewClient = getPaymentWebViewClient()

            loadHtml(html, domain)
        }
    }

    private fun requestPayment(paymentInfoPayload: String) {
        showLoading(true)

        paymentWebView.webViewClient = getPaymentWebViewClient {
            evaluateJavascript("javascript:$paymentInfoPayload", null)
        }
    }

    private fun showLoading(isShown: Boolean) {
        paymentWebView.visibility = if (isShown) View.GONE else View.VISIBLE
    }
}