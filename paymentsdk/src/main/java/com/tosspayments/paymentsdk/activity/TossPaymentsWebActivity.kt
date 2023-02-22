package com.tosspayments.paymentsdk.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.tosspayments.paymentsdk.R

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

    private lateinit var webView : WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_tosspayment)

        initViews()
    }

    private fun initViews() {
        webView = findViewById<WebView?>(R.id.webview_payment).apply {
            settings.javaScriptEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true

            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            webChromeClient = WebChromeClient()

            addJavascriptInterface(
                TossPaymentWebJavascriptInterface(),
                "TossPayments"
            )
        }
    }

    private inner class TossPaymentWebJavascriptInterface {
        @JavascriptInterface
        fun requestPayments(paymentDom: String) {

        }
    }
}