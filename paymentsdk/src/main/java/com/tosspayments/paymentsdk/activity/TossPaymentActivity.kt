package com.tosspayments.paymentsdk.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tosspayments.paymentsdk.R
import com.tosspayments.paymentsdk.TossPayments
import com.tosspayments.paymentsdk.interfaces.TossPaymentCallback
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentMethod
import com.tosspayments.paymentsdk.view.TossPaymentView
import org.json.JSONObject

internal class TossPaymentActivity : AppCompatActivity() {
    companion object {
        fun getIntent(
            context: Context,
            clientKey: String,
            method: TossPaymentMethod,
            tossPaymentInfo: TossPaymentInfo
        ): Intent {
            return Intent(context, TossPaymentActivity::class.java).apply {
                putExtra(TossPayments.EXTRA_CLIENT_KEY, clientKey)
                putExtra(TossPayments.EXTRA_METHOD, method.displayName)
                putExtra(TossPayments.EXTRA_PAYMENT_INFO, tossPaymentInfo.getPayload().toString())
            }
        }

        internal fun getIntent(context: Context, dom: String): Intent {
            return Intent(context, TossPaymentActivity::class.java)
                .putExtra(TossPayments.EXTRA_PAYMENT_DOM, dom)
        }
    }

    private var viewPayment: TossPaymentView? = null

    private val paymentCallback: TossPaymentCallback
        get() = object : TossPaymentCallback {
            override fun onSuccess(success: TossPaymentResult.Success) {
                setResult(
                    TossPayments.RESULT_PAYMENT_SUCCESS, Intent()
                        .putExtra(TossPayments.EXTRA_PAYMENT_RESULT_SUCCESS, success)
                )

                finish()
            }

            override fun onFailed(fail: TossPaymentResult.Fail) {
                setResult(
                    TossPayments.RESULT_PAYMENT_FAILED, Intent()
                        .putExtra(TossPayments.EXTRA_PAYMENT_RESULT_FAILED, fail)
                )

                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tosspayment)

        initViews()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun initViews() {
        viewPayment = findViewById<TossPaymentView>(R.id.payment_view).apply {
            callback = paymentCallback
        }
    }

    private fun handleIntent(intent: Intent?) {
        var orderId = ""
        val clientKey = intent?.getStringExtra(TossPayments.EXTRA_CLIENT_KEY)
        val methodName = intent?.getStringExtra(TossPayments.EXTRA_METHOD)
        val paymentPayload = intent?.getStringExtra(TossPayments.EXTRA_PAYMENT_INFO)?.also {
            orderId = kotlin.runCatching { JSONObject(it).get("orderId").toString() }.getOrNull()
                .orEmpty()
        }
        val paymentDom = intent?.getStringExtra(TossPayments.EXTRA_PAYMENT_DOM)
        val paymentCanceledMessage = "Payment has been canceled by the customer"

        val errorMessage = when {
            !paymentDom.isNullOrBlank() -> paymentCanceledMessage
            methodName.isNullOrBlank() -> "Method is empty"
            clientKey.isNullOrBlank() -> "ClientKey is empty"
            paymentPayload.isNullOrBlank() -> "PaymentInfo is empty"
            else -> paymentCanceledMessage
        }

        setResult(
            TossPayments.RESULT_PAYMENT_FAILED, Intent()
                .putExtra(
                    TossPayments.EXTRA_PAYMENT_RESULT_FAILED,
                    TossPaymentResult.Fail(
                        errorCode = "PAY_PROCESS_CANCELED",
                        errorMessage = errorMessage,
                        orderId = orderId
                    )
                )
        )

        when {
            !paymentDom.isNullOrBlank() -> {
                viewPayment?.requestPaymentFromDom(paymentDom) ?: kotlin.run { finish() }
            }
            !methodName.isNullOrBlank() && !clientKey.isNullOrBlank() && !paymentPayload.isNullOrBlank() -> {
                viewPayment?.requestPayment(clientKey, methodName, paymentPayload)
                    ?: kotlin.run { finish() }
            }
            else -> {
                finish()
            }
        }
    }
}