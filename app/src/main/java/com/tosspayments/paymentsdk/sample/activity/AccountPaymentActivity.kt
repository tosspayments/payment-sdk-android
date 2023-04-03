package com.tosspayments.paymentsdk.sample.activity

import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.KeyboardType
import com.tosspayments.paymentsdk.sample.viewmodel.AccountPaymentViewModel
import com.tosspayments.paymentsdk.model.paymentinfo.CashReceipt
import com.tosspayments.paymentsdk.model.paymentinfo.TossAccountPaymentInfo
import com.tosspayments.paymentsdk.sample.composable.ItemSelectDialog
import com.tosspayments.paymentsdk.sample.composable.PaymentInfoInput

class AccountPaymentActivity : PaymentActivity<TossAccountPaymentInfo>() {
    override val viewModel: AccountPaymentViewModel by viewModels()

    @Composable
    override fun ExtraPaymentInfo() {
        ValidHours()
        PhoneNumber()
        ShowPhoneNumber()
        CashReceipt()
    }

    @Composable
    private fun ValidHours() {
        PaymentInfoInput(
            labelText = "유효시간",
            initInputText = viewModel.validHours.collectAsState().value?.toString().orEmpty()
        ) {
            viewModel.setValidHours(it)
        }
    }

    @Composable
    private fun PhoneNumber() {
        PaymentInfoInput(
            labelText = "고객 휴대폰 번호",
            keyboardType = KeyboardType.Phone,
            initInputText = viewModel.customerMobilePhone.collectAsState().value.orEmpty()
        ) {
            viewModel.setPhoneNumber(it)
        }
    }

    @Composable
    private fun ShowPhoneNumber() {
        ItemSelectDialog(
            label = "고객 핸드폰 번호 표시",
            buttonText = when (viewModel.showCustomerMobilePhone.collectAsState().value) {
                true -> "표시"
                false -> "미표시"
                else -> "미설정"
            },
            items = listOf(Pair("미설정", null), Pair("표시", true), Pair("미표시", false))
        ) {
            viewModel.setShowCustomerPhoneNumber(it)
        }
    }

    @Composable
    private fun CashReceipt() {
        ItemSelectDialog(
            label = "현금영수증 발행",
            buttonText = viewModel.cashReceipt.collectAsState().value?.value ?: "미설정",
            items = CashReceipt.values().map { Pair(it.value, it) } + listOf(Pair("미설정", null))
        ) {
            viewModel.setCashReceipt(it)
        }
    }
}