package com.tosspayments.paymentsdk.sample.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.tosspayments.paymentsdk.PaymentWidget
import com.tosspayments.paymentsdk.model.PaymentWidgetOptions
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.sample.R
import com.tosspayments.paymentsdk.view.PaymentMethodWidget

class PaymentWidgetActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_KEY_AMOUNT = "extraKeyAmount"
        private const val EXTRA_KEY_CLIENT_KEY = "extraKeyClientKey"
        private const val EXTRA_KEY_CUSTOMER_KEY = "extraKeyCustomerKey"
        private const val EXTRA_KEY_ORDER_ID = "extraKeyOrderId"
        private const val EXTRA_KEY_ORDER_NAME = "extraKeyOrderName"
        private const val EXTRA_KEY_REDIRECT_URL = "extraKeyRedirectUrl"

        fun getIntent(
            context: Context,
            amount: Long,
            clientKey: String,
            customerKey: String,
            orderId: String,
            orderName: String,
            redirectUrl: String? = null
        ): Intent {
            return Intent(context, PaymentWidgetActivity::class.java)
                .putExtra(EXTRA_KEY_AMOUNT, amount)
                .putExtra(EXTRA_KEY_CLIENT_KEY, clientKey)
                .putExtra(EXTRA_KEY_CUSTOMER_KEY, customerKey)
                .putExtra(EXTRA_KEY_ORDER_ID, orderId)
                .putExtra(EXTRA_KEY_ORDER_NAME, orderName)
                .putExtra(EXTRA_KEY_REDIRECT_URL, redirectUrl)
        }
    }

    private val tossPaymentActivityResult: ActivityResultLauncher<Intent> =
        PaymentWidget.getPaymentResultLauncher(
            this,
            { success ->
                handlePaymentSuccessResult(success)
            },
            { fail ->
                handlePaymentFailResult(fail)
            })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_widget)

        intent?.run {
            initViews(
                getLongExtra(EXTRA_KEY_AMOUNT, 0),
                getStringExtra(EXTRA_KEY_CLIENT_KEY).orEmpty(),
                getStringExtra(EXTRA_KEY_CUSTOMER_KEY).orEmpty(),
                getStringExtra(EXTRA_KEY_ORDER_ID).orEmpty(),
                getStringExtra(EXTRA_KEY_ORDER_NAME).orEmpty(),
                getStringExtra(EXTRA_KEY_REDIRECT_URL),
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initViews(
        amount: Long,
        clientKey: String,
        customerKey: String,
        orderId: String,
        orderName: String,
        redirectUrl: String?
    ) {
        val paymentWidget = PaymentWidget(
            activity = this@PaymentWidgetActivity,
            clientKey = clientKey,
            customerKey = customerKey,
            options = redirectUrl?.let {
                PaymentWidgetOptions.Builder()
                    .brandPayOption(redirectUrl = it)
                    .build()
            }
        )

        val methodWidget = findViewById<PaymentMethodWidget>(R.id.payment_widget)
        paymentWidget.setMethodWidget(methodWidget)

        paymentWidget.renderPaymentMethodWidget(
            amount = amount,
            orderId = orderId
        )

        findViewById<Button>(R.id.request_payment_cta).setOnClickListener {
            paymentWidget.requestPayment(
                paymentResultLauncher = tossPaymentActivityResult,
                orderId = orderId,
                orderName = orderName
            )
        }
    }

    private fun handlePaymentSuccessResult(success: TossPaymentResult.Success) {
        startActivity(
            PaymentResultActivity.getIntent(
                this@PaymentWidgetActivity,
                true,
                arrayListOf(
                    "PaymentKey|${success.paymentKey}",
                    "OrderId|${success.orderId}",
                    "Amount|${success.amount}"
                )
            )
        )
    }

    private fun handlePaymentFailResult(fail: TossPaymentResult.Fail) {
        startActivity(
            PaymentResultActivity.getIntent(
                this@PaymentWidgetActivity,
                false,
                arrayListOf(
                    "ErrorCode|${fail.errorCode}",
                    "ErrorMessage|${fail.errorMessage}",
                    "OrderId|${fail.orderId}"
                )
            )
        )
    }
}