package com.tosspayments.paymentsdk.application.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.tosspayments.paymentsdk.model.paymentinfo.CashReceipt
import com.tosspayments.paymentsdk.model.paymentinfo.TossAccountPaymentInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccountPaymentViewModel : BasePaymentViewModel<TossAccountPaymentInfo>() {
    private val _validHours = MutableStateFlow<Int?>(null)
    val validHours = _validHours.asStateFlow()

    private val _customerMobilePhone = MutableStateFlow<String?>(null)
    val customerMobilePhone = _customerMobilePhone.asStateFlow()

    private val _showCustomerMobilePhone = MutableStateFlow<Boolean?>(null)
    val showCustomerMobilePhone = _showCustomerMobilePhone.asStateFlow()

    private val _cashReceipt = MutableStateFlow<CashReceipt?>(null)
    val cashReceipt = _cashReceipt.asStateFlow()

    override val paymentInfo: TossAccountPaymentInfo
        get() = TossAccountPaymentInfo(
            orderId = _orderId.value,
            orderName = _orderName.value,
            amount = _amount.value
        ).apply {
            this.customerName = _customerName.value
            this.customerEmail = _customerEmail.value
            this.taxFreeAmount = _taxFreeAmount.value

            this.validHours = _validHours.value
            this.customerMobilePhone = _customerMobilePhone.value
            this.showCustomerMobilePhone = _showCustomerMobilePhone.value
            this.cashReceipt = _cashReceipt.value?.value
        }

    fun setValidHours(validHours: String) {
        _validHours.value = kotlin.runCatching {
            720.coerceAtMost(validHours.toInt())
        }.getOrNull()
    }

    fun setPhoneNumber(phoneNumber: String?) {
        _customerMobilePhone.value = phoneNumber
    }

    fun setShowCustomerPhoneNumber(isShown: Boolean?) {
        _showCustomerMobilePhone.value = isShown
    }

    fun setCashReceipt(cashReceipt: CashReceipt?) {
        _cashReceipt.value = cashReceipt
    }

    override fun requestPayment(
        activity: Activity,
        resultLauncher: ActivityResultLauncher<Intent>
    ) {
        tossPayments.requestAccountPayment(activity, paymentInfo, resultLauncher)
    }
}