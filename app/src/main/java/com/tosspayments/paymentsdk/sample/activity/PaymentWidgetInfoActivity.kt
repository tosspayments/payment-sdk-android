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
import com.tosspayments.paymentsdk.sample.BuildConfig
import com.tosspayments.paymentsdk.sample.R
import com.tosspayments.paymentsdk.sample.viewmodel.PaymentWidgetInfoViewModel
import kotlinx.coroutines.launch

class PaymentWidgetInfoActivity : AppCompatActivity() {
    private val viewModel: PaymentWidgetInfoViewModel by viewModels()

    private lateinit var nextCta: Button

    companion object {
        private const val CUSTOMER_KEY = BuildConfig.CUSTOMER_KEY
        private const val CLIENT_KEY = BuildConfig.CLIENT_KEY
        private const val REDIRECT_URL = BuildConfig.REDIRECT_URL
        private const val ORDER_ID = BuildConfig.ORDER_ID
        private const val ORDER_NAME = "Kotlin IN ACTION 외 1권"
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

            setText(CLIENT_KEY)
        }

        findViewById<AppCompatEditText>(R.id.payment_cutomer_key).apply {
            addTextChangedListener {
                viewModel.setCustomerKey(it.toString())
            }

            setText(CUSTOMER_KEY)
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

            setText(ORDER_ID)
        }

        findViewById<EditText>(R.id.payment_order_name).run {
            addTextChangedListener {
                viewModel.setOrderName(it.toString())
            }

            setText(ORDER_NAME)
        }

        findViewById<EditText>(R.id.payment_redirect_url).run {
            addTextChangedListener {
                viewModel.setRedirectUrl(it.toString())
            }

            setText(REDIRECT_URL)
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