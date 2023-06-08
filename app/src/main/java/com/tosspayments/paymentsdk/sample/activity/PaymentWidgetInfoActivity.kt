package com.tosspayments.paymentsdk.sample.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.tosspayments.paymentsdk.sample.R
import com.tosspayments.paymentsdk.sample.viewmodel.PaymentWidgetInfoViewModel
import kotlinx.coroutines.launch

class PaymentWidgetInfoActivity : AppCompatActivity() {
    private val viewModel: PaymentWidgetInfoViewModel by viewModels()

    private lateinit var nextCta: Button

    companion object {
        private const val DEFAULT_CUSTOMER_KEY = "test_sk_O6BYq7GWPVvMGnqb4143NE5vbo1d"
        private const val DEFAULT_CLIENT_KEY = "test_ck_0Poxy1XQL8R4P1zpv14V7nO5Wmlg"
        private const val DEFAULT_ORDER_ID = "kangdroid"
        private const val DEFAULT_ORDER_NAME = "Kotlin IN ACTION 외 2권"
        private const val DEFAULT_REDIRECT_URL = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_widget_info)

        initViews()
        bindViewModel()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        findViewById<AppCompatEditText>(R.id.payment_client_key).apply {
            addTextChangedListener {
                viewModel.setClientKey(it.toString())
            }

            setText(DEFAULT_CLIENT_KEY)
        }

        findViewById<AppCompatEditText>(R.id.payment_cutomer_key).apply {
            addTextChangedListener {
                viewModel.setCustomerKey(it.toString())
            }

            setText(DEFAULT_CUSTOMER_KEY)
        }

        findViewById<EditText>(R.id.payment_amount).apply {
            addTextChangedListener {
                val amount = try {
                    it.toString().toLong()
                } catch (e: Exception) {
                    0L
                }

                viewModel.setAmount(amount)
            }

            setText("50000")
        }

        findViewById<EditText>(R.id.payment_order_Id).apply {
            addTextChangedListener {
                viewModel.setOrderId(it.toString())
            }

            setText(DEFAULT_ORDER_ID)
        }

        findViewById<EditText>(R.id.payment_order_name).run {
            addTextChangedListener {
                viewModel.setOrderName(it.toString())
            }

            setText(DEFAULT_ORDER_NAME)
        }

        findViewById<EditText>(R.id.payment_redirect_url).run {
            addTextChangedListener {
                viewModel.setRedirectUrl(it.toString())
            }

            setText(DEFAULT_REDIRECT_URL)
        }

        nextCta = findViewById(R.id.payment_next_cta)
    }

    private fun bindViewModel() {
        lifecycleScope.launch {
            viewModel.paymentEnableState.collect { uiState ->
                nextCta.isEnabled = uiState != PaymentWidgetInfoViewModel.UiState.Invalid

                val (isEnabled, clickListener) = when (uiState) {
                    is PaymentWidgetInfoViewModel.UiState.Invalid -> {
                        Pair(false, null)
                    }
                    is PaymentWidgetInfoViewModel.UiState.Valid -> {
                        Pair(true, object : View.OnClickListener {
                            override fun onClick(v: View?) {
                                startActivity(
                                    PaymentWidgetActivity.getIntent(
                                        this@PaymentWidgetInfoActivity,
                                        amount = uiState.amount,
                                        clientKey = uiState.clientKey,
                                        customerKey = uiState.customerKey,
                                        orderId = uiState.orderId,
                                        orderName = uiState.orderName,
                                        redirectUrl = uiState.redirectUrl
                                    )
                                )
                            }
                        })
                    }
                }

                nextCta.run {
                    this.isEnabled = isEnabled
                    setOnClickListener(clickListener)
                }
            }
        }
    }
}