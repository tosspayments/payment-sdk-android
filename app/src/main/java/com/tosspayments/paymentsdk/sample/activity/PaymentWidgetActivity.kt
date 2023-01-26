package com.tosspayments.paymentsdk.sample.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.tosspayments.paymentsdk.PaymentWidget
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.sample.R
import com.tosspayments.paymentsdk.sample.extension.hideKeyboard
import com.tosspayments.paymentsdk.sample.viewmodel.PaymentWidgetViewModel
import com.tosspayments.paymentsdk.view.PaymentMethodWidget
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class PaymentWidgetActivity : AppCompatActivity() {
    private val viewModel: PaymentWidgetViewModel by viewModels()

    private var paymentWidget: PaymentWidget? = null

    private lateinit var inputClientKey: AppCompatAutoCompleteTextView
    private lateinit var inputOrderId: AppCompatAutoCompleteTextView
    private lateinit var inputAmount: EditText
    private lateinit var paymentCta: Button
    private lateinit var methodWidget: PaymentMethodWidget

    private val amount: Long
        get() = try {
            inputAmount.text.toString().toLong()
        } catch (e: Exception) {
            0L
        }

    companion object {
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
        methodWidget = findViewById(R.id.payment_widget)

        paymentCta = findViewById(R.id.request_payment_cta)

        inputClientKey = findViewById(R.id.payment_client_key)

        inputAmount = findViewById<EditText>(R.id.payment_amount).apply {
            setText("50000")
        }

        inputOrderId = findViewById(R.id.payment_order_Id)

        findViewById<EditText>(R.id.payment_order_name).run {
            addTextChangedListener {
                viewModel.setOrderName(it.toString())
            }

            setText("Kotlin IN ACTION 외 1권")
        }

        findViewById<Button>(R.id.payment_client_key_confirm)?.setOnClickListener {
            setClientKey()
            hideKeyboard()
        }

        findViewById<Button>(R.id.payment_amount_confirm)?.setOnClickListener {
            setAmount()
            hideKeyboard()
        }

        findViewById<Button>(R.id.payment_orderId_confirm)?.setOnClickListener {
            viewModel.setOrderId(inputOrderId.text.toString())
            hideKeyboard()
        }

        setAmount()
    }

    private fun setClientKey() {
        viewModel.setClientKey(inputClientKey.text.toString())
    }

    private fun setAmount() {
        viewModel.setAmount(amount)
    }

    @OptIn(FlowPreview::class)
    private fun bindViewModel() {
        lifecycleScope.launch {
            viewModel.clientKey.collect { clientKey ->
                inputClientKey.setText(clientKey)

                paymentWidget = PaymentWidget(clientKey, CUSTOMER_KEY)
                paymentWidget?.setMethodWidget(methodWidget)
                renderMethodWidget(amount)
            }
        }

        lifecycleScope.launch {
            viewModel.orderId.collectLatest {
                inputOrderId.setText(it)
            }
        }

        lifecycleScope.launch {
            viewModel.orderIdList.collectLatest { orderIdList ->
                inputOrderId.setAdapter(
                    ArrayAdapter(
                        this@PaymentWidgetActivity,
                        android.R.layout.simple_list_item_1,
                        orderIdList.filter { it.isNotBlank() }
                    )
                )
            }
        }

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

        lifecycleScope.launch {
            viewModel.clientKeyList.collectLatest { clientKeyList ->
                inputClientKey.setAdapter(
                    ArrayAdapter(
                        this@PaymentWidgetActivity,
                        android.R.layout.simple_list_item_1,
                        clientKeyList.filter { it.isNotBlank() }
                    )
                )
            }
        }
    }

    private fun renderMethodWidget(amount: Long) {
        paymentWidget?.renderPaymentMethodWidget(amount)
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
                        paymentWidget?.requestPayment(
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