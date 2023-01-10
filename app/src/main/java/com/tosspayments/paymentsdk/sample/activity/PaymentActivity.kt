package com.tosspayments.paymentsdk.sample.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tosspayments.paymentsdk.TossPayments
import com.tosspayments.paymentsdk.sample.viewmodel.BasePaymentViewModel
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo
import com.tosspayments.paymentsdk.sample.composable.CtaButton
import com.tosspayments.paymentsdk.sample.composable.PaymentInfoInput
import com.tosspayments.paymentsdk.sample.model.PaymentUiState

abstract class PaymentActivity<K : TossPaymentInfo> : AppCompatActivity() {
    private val tossPaymentActivityResult: ActivityResultLauncher<Intent> =
        TossPayments.getPaymentResultLauncher(
            this@PaymentActivity,
            { success ->
                handlePaymentSuccessResult(success)
            },
            { fail ->
                handlePaymentFailResult(fail)
            })

    abstract val viewModel: BasePaymentViewModel<K>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PaymentScreen()
        }
    }

    @Composable
    private fun PaymentScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
                .padding(24.dp, 12.dp, 24.dp, 12.dp)
        ) {
            PaymentInfo(
                Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 12.dp)
            )

            ConfirmPayment()
        }
    }

    @Composable
    private fun PaymentInfo(
        modifier: Modifier
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ClientKey()
            Amount()
            OrderId()
            OrderName()
            ExtraPaymentInfo()

            CustomerName()
            CustomerEmail()
            TaxFreeAmount()
        }
    }

    @Composable
    protected open fun ExtraPaymentInfo() {
    }

    @Composable
    private fun ClientKey() {
        PaymentInfoInput(
            labelText = "ClientKey",
            initInputText = viewModel.clientKey.collectAsState().value
        ) {
            viewModel.setClientKey(it)
        }
    }

    @Composable
    private fun Amount() {
        PaymentInfoInput(
            labelText = "Amount",
            initInputText = viewModel.amount.collectAsState().value.toString(),
            keyboardType = KeyboardType.Decimal
        ) {
            viewModel.setAmount(it)
        }
    }

    @Composable
    private fun OrderId() {
        PaymentInfoInput(
            labelText = "OrderId",
            initInputText = viewModel.orderId.collectAsState().value
        ) {
            viewModel.setOrderId(it)
        }
    }

    @Composable
    private fun OrderName() {
        PaymentInfoInput(
            labelText = "OrderName",
            initInputText = viewModel.orderName.collectAsState().value
        ) {
            viewModel.setOrderName(it)
        }
    }

    @Composable
    private fun CustomerName() {
        PaymentInfoInput(
            labelText = "고객명",
            initInputText = viewModel.customerName.collectAsState().value
        ) {
            viewModel.setCustomerName(it)
        }
    }

    @Composable
    private fun CustomerEmail() {
        PaymentInfoInput(
            labelText = "고객 Email",
            initInputText = viewModel.customerEmail.collectAsState().value,
            keyboardType = KeyboardType.Email
        ) {
            viewModel.setCustomerEmail(it)
        }
    }

    @Composable
    private fun TaxFreeAmount() {
        PaymentInfoInput(
            labelText = "TaxFree Amount",
            initInputText = viewModel.taxFreeAmount.collectAsState().value?.toString().orEmpty(),
            keyboardType = KeyboardType.Email
        ) {
            viewModel.setTaxFreeAmount(it)
        }
    }

    @Composable
    private fun ConfirmPayment() {
        val uiState = viewModel.uiState.collectAsState().value

        CtaButton(
            text = "결제하기",
            isEnabled = uiState != PaymentUiState.Edit,
            modifier = Modifier.fillMaxWidth()
        ) {
            (uiState as? PaymentUiState.Ready)?.let {
                viewModel.requestPayment(this@PaymentActivity, tossPaymentActivityResult)
            }
        }
    }

    private fun handlePaymentSuccessResult(success: TossPaymentResult.Success) {
        startActivity(
            PaymentResultActivity.getIntent(
                this@PaymentActivity,
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
                this@PaymentActivity,
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