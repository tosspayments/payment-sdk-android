package com.tosspayments.paymentsdk.application.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.tosspayments.paymentsdk.model.paymentinfo.TossMobilePaymentInfo
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentMobileCarrier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MobilePaymentViewModel : BasePaymentViewModel<TossMobilePaymentInfo>() {
    private val _mobileCarrier = MutableStateFlow<List<TossPaymentMobileCarrier>?>(null)
    val mobileCarrier = _mobileCarrier.asStateFlow()

    fun selectMobileCarrier(mobileCarrierCode: TossPaymentMobileCarrier?) {
        if (mobileCarrierCode != null) {
            val mobileCarrierList = _mobileCarrier.value.orEmpty().toMutableList()

            if (mobileCarrierList.contains(mobileCarrierCode)) {
                mobileCarrierList.remove(mobileCarrierCode)
            } else {
                mobileCarrierList.add(mobileCarrierCode)
            }

            _mobileCarrier.value = mobileCarrierList
        }
    }

    override val paymentInfo: TossMobilePaymentInfo
        get() = TossMobilePaymentInfo(
            orderId = _orderId.value,
            orderName = _orderName.value,
            amount = _amount.value
        ).apply {
            this.customerName = _customerName.value
            this.customerEmail = _customerEmail.value
            this.taxFreeAmount = _taxFreeAmount.value
            this.mobileCarrierList = _mobileCarrier.value
        }

    override fun requestPayment(
        activity: Activity,
        resultLauncher: ActivityResultLauncher<Intent>
    ) {
        tossPayments.requestMobilePayment(activity, paymentInfo, resultLauncher)
    }
}