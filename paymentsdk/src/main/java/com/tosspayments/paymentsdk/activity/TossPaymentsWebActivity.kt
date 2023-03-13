package com.tosspayments.paymentsdk.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebChromeClient
import androidx.appcompat.app.AppCompatActivity
import com.tosspayments.paymentsdk.R
import com.tosspayments.paymentsdk.interfaces.PaymentWidgetCallback
import com.tosspayments.paymentsdk.view.PaymentWebView
import com.tosspayments.paymentsdk.view.PaymentWebView.Companion.JS_INTERFACE_NAME

internal class TossPaymentsWebActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_KEY_DATA = "extraKeyTossPaymentWebData"

        fun getIntent(context: Context, data: String): Intent {
            return Intent(context, TossPaymentsWebActivity::class.java).putExtra(
                EXTRA_KEY_DATA,
                data
            )
        }
    }

    private lateinit var webView: PaymentWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_tosspayment)

        initViews(intent)
        handleIntent(intent)
    }

    private fun initViews(intent: Intent?) {
        webView = findViewById<PaymentWebView>(R.id.webview_payment).apply {
            webChromeClient = WebChromeClient()

            addJavascriptInterface(PaymentWebView.PaymentWebViewJavascriptInterface(object :
                PaymentWidgetCallback {
                override fun onPaymentDomCreated(html: String) {

                }

                override fun onHtmlRequested(html: String) {

                }

                override fun onHtmlRequestSucceeded(html: String) {
                    setResult(RESULT_OK, Intent().putExtra(EXTRA_KEY_DATA, html))
                    finish()
                }
            }), JS_INTERFACE_NAME)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        webView.loadHtml(intent?.getStringExtra(EXTRA_KEY_DATA).orEmpty())
    }
}