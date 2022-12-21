package com.tosspayments.paymentsdk.application.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.tosspayments.paymentsdk.application.R
import com.tosspayments.paymentsdk.application.viewmodel.PaymentWidgetViewModel
import com.tosspayments.paymentsdk.view.PaymentMethodWidget
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PaymentWidgetActivity : AppCompatActivity() {
    private val viewModel: PaymentWidgetViewModel by viewModels()

    private lateinit var paymentWidget: PaymentMethodWidget
    private lateinit var paymentCta: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_widget)

        initViews()
        bindViewModel()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        paymentWidget = findViewById(R.id.payment_widget)
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
    }

    private fun bindViewModel() {
        lifecycleScope.launch {
            viewModel.amount.collectLatest {
                paymentWidget.renderPaymentMethods(
                    "test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq",
                    "toss-payment",
                    it
                )
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest {uiState ->
                when(uiState) {
                    is PaymentWidgetViewModel.UiState.Invalid -> {
                        paymentCta.isEnabled = false
                    }
                    is PaymentWidgetViewModel.UiState.Valid -> {
                        paymentCta.run {
                            isEnabled = true

                            setOnClickListener {
                                paymentWidget.requestPayment(uiState.orderId, uiState.orderName)
                            }
                        }
                    }
                }
            }
        }
    }
}