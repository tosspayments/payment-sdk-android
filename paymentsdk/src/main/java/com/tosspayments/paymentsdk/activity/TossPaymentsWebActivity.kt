package com.tosspayments.paymentsdk.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.JavascriptInterface
import androidx.appcompat.app.AppCompatActivity
import com.tosspayments.paymentsdk.R
import com.tosspayments.paymentsdk.interfaces.PaymentJavascriptInterface
import com.tosspayments.paymentsdk.model.Constants
import com.tosspayments.paymentsdk.view.PaymentWebView
import com.tosspayments.paymentsdk.view.PaymentWidgetContainer

internal class TossPaymentsWebActivity : AppCompatActivity() {
    companion object {
        fun getIntent(context: Context, domain: String?, data: String): Intent {
            return Intent(context, TossPaymentsWebActivity::class.java)
                .putExtra(Constants.EXTRA_KEY_DOMAIN, domain)
                .putExtra(Constants.EXTRA_KEY_DATA, data)
        }
    }

    private lateinit var webView: PaymentWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_tosspayment)

        initViews()
        handleIntent(intent)
    }

    private fun initViews() {
        webView = findViewById<PaymentWebView>(R.id.webview_payment).apply {
            addJavascriptInterface(object : PaymentJavascriptInterface {
                @JavascriptInterface
                fun success(response: String) {
                    handleSuccessResponse(response)
                }
            }, PaymentWidgetContainer.INTERFACE_NAME_WIDGET)

            addJavascriptInterface(object : PaymentJavascriptInterface {
                @JavascriptInterface
                fun success(response: String) {
                    handleSuccessResponse(response)
                }
            }, PaymentWebView.INTERFACE_NAME_PAYMENT)
        }
    }

    private fun handleSuccessResponse(response: String) {
        setResult(RESULT_OK, Intent().putExtra(Constants.EXTRA_KEY_DATA, response))
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val html = intent?.getStringExtra(Constants.EXTRA_KEY_DATA).orEmpty()
        val domain = intent?.getStringExtra(Constants.EXTRA_KEY_DOMAIN).orEmpty()
        webView.loadHtml(html, domain)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }
}