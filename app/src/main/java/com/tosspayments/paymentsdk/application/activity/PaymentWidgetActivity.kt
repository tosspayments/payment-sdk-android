package com.tosspayments.paymentsdk.application.activity

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.tosspayments.paymentsdk.application.R
import com.tosspayments.paymentsdk.view.PaymentWidget

class PaymentWidgetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_widget)

        WebView.setWebContentsDebuggingEnabled(true)

        renderWidget()
    }

    private fun renderWidget() {
        findViewById<PaymentWidget>(R.id.payment_widget).renderPaymentMethods(
            "test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq",
            "toss-payment",
            50000
        )
    }
}