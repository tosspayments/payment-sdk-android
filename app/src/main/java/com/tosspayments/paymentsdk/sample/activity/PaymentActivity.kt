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
import com.tosspayments.paymentsdk.model.paymentinfo.SubOrder
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo
import com.tosspayments.paymentsdk.sample.composable.CtaButton
import com.tosspayments.paymentsdk.sample.composable.Label
import com.tosspayments.paymentsdk.sample.composable.OutlineButton
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
                .statusBarsPadding()
                .imePadding()
                .navigationBarsPadding()
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
            SubOrders()
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
    private fun SubOrders() {
        val subOrders = viewModel.subOrders.collectAsState().value

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Label("SubOrders")

            subOrders.forEachIndexed { index, subOrder ->
                SubOrderInput(index, subOrder)
            }

            OutlineButton(
                text = "SubOrder 추가",
                modifier = Modifier.fillMaxWidth()
            ) {
                viewModel.addSubOrder()
            }
        }
    }

    @Composable
    private fun SubOrderInput(index: Int, subOrder: SubOrder) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Label("SubOrder ${index + 1}")

            PaymentInfoInput(
                labelText = "사업자등록번호",
                initInputText = subOrder.merchantBusinessNumber
            ) {
                viewModel.updateSubOrder(index, subOrder.copy(merchantBusinessNumber = it))
            }

            PaymentInfoInput(
                labelText = "상점명",
                initInputText = subOrder.merchantName
            ) {
                viewModel.updateSubOrder(index, subOrder.copy(merchantName = it))
            }

            PaymentInfoInput(
                labelText = "국가 코드",
                initInputText = subOrder.merchantAddress.country
            ) {
                viewModel.updateSubOrder(
                    index,
                    subOrder.copy(
                        merchantAddress = subOrder.merchantAddress.copy(country = it)
                    )
                )
            }

            PaymentInfoInput(
                labelText = "우편번호",
                initInputText = subOrder.merchantAddress.postalCode
            ) {
                viewModel.updateSubOrder(
                    index,
                    subOrder.copy(
                        merchantAddress = subOrder.merchantAddress.copy(postalCode = it)
                    )
                )
            }

            PaymentInfoInput(
                labelText = "주소",
                initInputText = subOrder.merchantAddress.address
            ) {
                viewModel.updateSubOrder(
                    index,
                    subOrder.copy(
                        merchantAddress = subOrder.merchantAddress.copy(address = it)
                    )
                )
            }

            PaymentInfoInput(
                labelText = "상세주소",
                initInputText = subOrder.merchantAddress.detailAddress.orEmpty()
            ) {
                viewModel.updateSubOrder(
                    index,
                    subOrder.copy(
                        merchantAddress = subOrder.merchantAddress.copy(
                            detailAddress = it.takeIf { detailAddress -> detailAddress.isNotBlank() }
                        )
                    )
                )
            }

            PaymentInfoInput(
                labelText = "하위 주문명",
                initInputText = subOrder.orderName
            ) {
                viewModel.updateSubOrder(index, subOrder.copy(orderName = it))
            }

            OutlineButton(
                text = "SubOrder 삭제",
                modifier = Modifier.fillMaxWidth()
            ) {
                viewModel.removeSubOrder(index)
            }
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
