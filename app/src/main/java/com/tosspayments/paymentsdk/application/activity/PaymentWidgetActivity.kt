package com.tosspayments.paymentsdk.application.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.tosspayments.paymentsdk.PaymentWidget
import com.tosspayments.paymentsdk.TossPayments
import com.tosspayments.paymentsdk.application.R
import com.tosspayments.paymentsdk.application.viewmodel.PaymentWidgetViewModel
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.view.PaymentMethodWidget
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PaymentWidgetActivity : AppCompatActivity() {
    private val viewModel: PaymentWidgetViewModel by viewModels()

    private lateinit var methodWidget: PaymentMethodWidget
    private lateinit var paymentCta: Button

    private val paymentWidget = PaymentWidget(CLIENT_KEY)

    companion object {
        private const val CLIENT_KEY = "test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq"
        private const val CUSTOMER_KEY = "toss-payment"
    }

    private val tossPaymentActivityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                TossPayments.RESULT_PAYMENT_SUCCESS -> {
                    result.data?.getParcelableExtra<TossPaymentResult.Success>(TossPayments.EXTRA_PAYMENT_RESULT_SUCCESS)
                        ?.let { success ->
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
                }
                TossPayments.RESULT_PAYMENT_FAILED -> {
                    result.data?.getParcelableExtra<TossPaymentResult.Fail>(TossPayments.EXTRA_PAYMENT_RESULT_FAILED)
                        ?.let { fail ->
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
                else -> {}
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_widget)

        initViews()
        bindViewModel()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        methodWidget = findViewById(R.id.payment_widget)
        paymentCta = findViewById(R.id.request_payment_cta)

        findViewById<EditText>(R.id.payment_amount).apply {
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

        findViewById<EditText>(R.id.payment_order_Id).apply {
            addTextChangedListener {
                viewModel.setOrderId(it.toString())
            }

            setText("AD8aZDpbzXs4EQa")
        }

        findViewById<EditText>(R.id.payment_order_name).apply {
            addTextChangedListener {
                viewModel.setOrderName(it.toString())
            }

            setText("리팩터링 2판 외 1권")
        }

        paymentWidget.setMethodWidget(methodWidget)
    }

    private fun bindViewModel() {
        lifecycleScope.launch {
            viewModel.amount.collectLatest {
                paymentWidget.renderPaymentMethodWidget(
                    CUSTOMER_KEY,
                    it
                )
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { uiState ->
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
        }
    }
}