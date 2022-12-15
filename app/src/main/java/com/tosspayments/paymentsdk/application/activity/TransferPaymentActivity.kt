package com.tosspayments.paymentsdk.application.activity

import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.tosspayments.paymentsdk.application.composable.ItemSelectDialog
import com.tosspayments.paymentsdk.model.paymentinfo.CashReceipt
import com.tosspayments.paymentsdk.model.paymentinfo.TossTransferPaymentInfo
import com.tosspayments.paymentsdk.application.viewmodel.TransferPaymentViewModel

class TransferPaymentActivity : PaymentActivity<TossTransferPaymentInfo>() {
    override val viewModel: TransferPaymentViewModel by viewModels()

    @Composable
    override fun ExtraPaymentInfo() {
        CashReceipt()
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