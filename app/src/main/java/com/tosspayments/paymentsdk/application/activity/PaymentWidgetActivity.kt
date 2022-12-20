package com.tosspayments.paymentsdk.application.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.tosspayments.paymentsdk.application.R
import com.tosspayments.paymentsdk.view.PaymentMethodWidget

class PaymentWidgetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_widget)

        initViews()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        var orderId = ""
        var orderName = ""

        findViewById<EditText>(R.id.payment_order_Id).apply {
            addTextChangedListener {
                orderId = it.toString()
            }

            setText("AD8aZDpbzXs4EQa")
        }

        findViewById<EditText>(R.id.payment_order_name).apply {
            addTextChangedListener {
                orderName = it.toString()
            }

            setText("리팩터링 2판 외 1권")
        }

        val paymentWidget = findViewById<PaymentMethodWidget>(R.id.payment_widget).apply {
            renderPaymentMethods(
                "test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq",
                "toss-payment",
                50000
            )
        }

        findViewById<Button>(R.id.request_payment_cta).setOnClickListener {
            paymentWidget.requestPayment(orderId, orderName)
        }
    }
}