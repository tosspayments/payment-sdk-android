package com.tosspayments.paymentsdk.application.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GiftCertificatePaymentViewModel : BasePaymentViewModel<TossPaymentInfo>() {
    private val _method: MutableStateFlow<TossPaymentMethod.GiftCertificate> =
        MutableStateFlow(TossPaymentMethod.GiftCertificate.Culture)
    val method = _method.asStateFlow()

    override val paymentInfo: TossPaymentInfo
        get() = TossPaymentInfo(
            orderId = _orderId.value,
            orderName = _orderName.value,
            amount = _amount.value
        ).apply {
            this.customerName = _customerName.value
            this.customerEmail = _customerEmail.value
            this.taxFreeAmount = _taxFreeAmount.value
        }

    fun setMethod(method: TossPaymentMethod.GiftCertificate) {
        _method.value = method
    }

    override fun requestPayment(
        activity: Activity,
        resultLauncher: ActivityResultLauncher<Intent>
    ) {
        tossPayments.requestGiftCertificatePayment(
            activity,
            method.value,
            paymentInfo,
            resultLauncher
        )
    }
}