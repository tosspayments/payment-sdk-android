package com.tosspayments.paymentsdk.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.tosspayments.paymentsdk.R
import com.tosspayments.paymentsdk.interfaces.PaymentWidgetCallback
import com.tosspayments.paymentsdk.model.Constants
import com.tosspayments.paymentsdk.view.PaymentWebView
import com.tosspayments.paymentsdk.view.PaymentWebView.Companion.JS_INTERFACE_NAME

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

        initViews(intent)
        handleIntent(intent)
    }

    private fun initViews(intent: Intent?) {
        webView = findViewById<PaymentWebView>(R.id.webview_payment).apply {
            val domain = intent?.getStringExtra(Constants.EXTRA_KEY_DOMAIN)

            addJavascriptInterface(
                PaymentWebView.PaymentWebViewJavascriptInterface(object :
                    PaymentWidgetCallback {
                    override fun onPostPaymentHtml(html: String, domain: String?) {
                    }

                    override fun onHtmlRequested(html: String, domain: String?) {
                    }

                    override fun onSuccess(response: String, domain: String?) {
                        setResult(RESULT_OK, Intent().putExtra(Constants.EXTRA_KEY_DATA, response))
                        finish()
                    }
                }, domain), JS_INTERFACE_NAME
            )
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        webView.loadHtml(
            intent?.getStringExtra(Constants.EXTRA_KEY_DATA).orEmpty(),
            intent?.getStringExtra(Constants.EXTRA_KEY_DOMAIN)
        )
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