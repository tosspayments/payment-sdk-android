package com.tosspayments.paymentsdk.application.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.tosspayments.paymentsdk.model.paymentinfo.CashReceipt
import com.tosspayments.paymentsdk.model.paymentinfo.TossTransferPaymentInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TransferPaymentViewModel : BasePaymentViewModel<TossTransferPaymentInfo>() {
    private val _cashReceipt = MutableStateFlow<CashReceipt?>(null)
    val cashReceipt = _cashReceipt.asStateFlow()

    override val paymentInfo: TossTransferPaymentInfo
        get() = TossTransferPaymentInfo(
            orderId = _orderId.value,
            orderName = _orderName.value,
            amount = _amount.value
        ).apply {
            this.customerName = _customerName.value
            this.customerEmail = _customerEmail.value
            this.taxFreeAmount = _taxFreeAmount.value

            this.cashReceipt = _cashReceipt.value?.value
        }

    fun setCashReceipt(cashReceipt: CashReceipt?) {
        _cashReceipt.value = cashReceipt
    }

    override fun requestPayment(
        activity: Activity,
        resultLauncher: ActivityResultLauncher<Intent>
    ) {
        tossPayments.requestTransferPayment(activity, paymentInfo, resultLauncher)
    }
}