package com.tosspayments.paymentsdk.sample.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.tosspayments.paymentsdk.PaymentWidget
import com.tosspayments.paymentsdk.sample.viewmodel.PaymentWidgetViewModel
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.sample.R
import com.tosspayments.paymentsdk.view.PaymentMethodWidget
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class PaymentWidgetActivity : AppCompatActivity() {
    private val viewModel: PaymentWidgetViewModel by viewModels()
    private val paymentWidget = PaymentWidget(CLIENT_KEY)

    private lateinit var paymentCta: Button

    companion object {
        private const val CLIENT_KEY = "live_ck_D4yKeq5bgrpn2v0D4yp3GX0lzW6Y"
        private const val CUSTOMER_KEY = "toss-payment"
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

        initViews()
        bindViewModel()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        val methodWidget = findViewById<PaymentMethodWidget>(R.id.payment_widget)
        paymentWidget.setMethodWidget(methodWidget)

        paymentCta = findViewById(R.id.request_payment_cta)

        findViewById<EditText>(R.id.payment_amount).run {
            addTextChangedListener {
                viewModel.setAmount(
                    try {
                        it?.toString()?.toLong() ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                )
            }

            setText("50000")
        }

        findViewById<EditText>(R.id.payment_order_Id).run {
            addTextChangedListener {
                viewModel.setOrderId(it.toString())
            }

            setText("AD8aZDpbzXs4EQa")
        }

        findViewById<EditText>(R.id.payment_order_name).run {
            addTextChangedListener {
                viewModel.setOrderName(it.toString())
            }

            setText("Kotlin IN ACTION 외 1권")
        }
    }

    @OptIn(FlowPreview::class)
    private fun bindViewModel() {
        lifecycleScope.launch {
            viewModel.amount.debounce(300).distinctUntilChanged().collectLatest {
                renderMethodWidget(it)
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { uiState ->
                handleUiState(uiState)
            }
        }
    }

    private fun renderMethodWidget(amount: Long) {
        paymentWidget.renderPaymentMethodWidget(CUSTOMER_KEY, amount)
    }

    private fun handleUiState(uiState: PaymentWidgetViewModel.UiState) {
        when (uiState) {
            is PaymentWidgetViewModel.UiState.Invalid -> {
                paymentCta.isEnabled = false
            }
            is PaymentWidgetViewModel.UiState.Valid -> {
                paymentCta.run {
                    isEnabled = true

                    setOnClickListener {
                        paymentWidget.requestPayment(
                            paymentResultLauncher = tossPaymentActivityResult,
                            orderId = uiState.orderId,
                            orderName = uiState.orderName,
                        )
                    }
                }
            }
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