package com.tosspayments.paymentsdk.sample.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.tosspayments.paymentsdk.model.paymentinfo.TossCardPaymentCompany
import com.tosspayments.paymentsdk.model.paymentinfo.TossCardPaymentFlow
import com.tosspayments.paymentsdk.model.paymentinfo.TossCardPaymentInfo
import com.tosspayments.paymentsdk.model.paymentinfo.TossEasyPayCompany
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CardPaymentViewModel : BasePaymentViewModel<TossCardPaymentInfo>() {
    override val paymentInfo: TossCardPaymentInfo
        get() = TossCardPaymentInfo(
            orderId = _orderId.value,
            orderName = _orderName.value,
            amount = _amount.value
        ).apply {
            this.customerName = _customerName.value
            this.customerEmail = _customerEmail.value
            this.taxFreeAmount = _taxFreeAmount.value

            this.cardCompany = _cardCompany.value
            this.cardInstallmentPlan = _installmentPlan.value
            this.maxCardInstallmentPlan = _maxInstallmentPlan.value
            this.useCardPoint = _useCardPoint.value
            this.useAppCardOnly = _useAppCardOnly.value
            this.useInternationalCardOnly = _useInternationalCardOnly.value
            this.flowMode = _flowMode.value
            this.easyPay = _easyPay.value
            this.discountCode = _discountCode.value
        }

    private val _cardCompany = MutableStateFlow<TossCardPaymentCompany?>(null)
    val cardCompany = _cardCompany.asStateFlow()

    private val _installmentPlan = MutableStateFlow<Int?>(null)
    val installmentPlan = _installmentPlan.asStateFlow()

    private val _maxInstallmentPlan = MutableStateFlow<Int?>(null)
    val maxInstallmentPlan = _maxInstallmentPlan.asStateFlow()

    private val _useCardPoint = MutableStateFlow<Boolean?>(null)
    val useCardPoint = _useCardPoint.asStateFlow()

    private val _useAppCardOnly = MutableStateFlow<Boolean?>(null)
    val useAppCardOnly = _useAppCardOnly.asStateFlow()

    private val _useInternationalCardOnly = MutableStateFlow<Boolean?>(null)
    val useInternationalCardOnly = _useInternationalCardOnly.asStateFlow()

    private val _flowMode = MutableStateFlow(TossCardPaymentFlow.DEFAULT)
    val flowMode = _flowMode.asStateFlow()

    private val _discountCode = MutableStateFlow<String?>(null)
    val discountCode = _discountCode.asStateFlow()

    private val _easyPay = MutableStateFlow<TossEasyPayCompany?>(null)
    val easyPay = _easyPay.asStateFlow()

    fun setCardCompany(cardCompany: TossCardPaymentCompany?) {
        _cardCompany.value = cardCompany
    }

    fun setInstallmentPlan(installmentPlan: Int?) {
        _installmentPlan.value = installmentPlan
    }

    fun setMaxInstallmentPlan(maxInstallmentPlan: Int?) {
        _maxInstallmentPlan.value = maxInstallmentPlan
    }

    fun setUseCardPoint(useCardPoint: Boolean?) {
        _useCardPoint.value = useCardPoint
    }

    fun setUseAppCardOnly(useAppCardOnly: Boolean?) {
        _useAppCardOnly.value = useAppCardOnly
    }

    fun setUseInternationalCardOnly(useInternationalCardOnly: Boolean?) {
        _useInternationalCardOnly.value = useInternationalCardOnly
    }

    fun setFlowMode(flowMode: TossCardPaymentFlow) {
        _flowMode.value = flowMode
    }

    fun setEasyPay(easyPay: TossEasyPayCompany?) {
        _easyPay.value = easyPay
    }

    fun setDiscountCode(discountCode: String?) {
        _discountCode.value = discountCode
    }

    override fun requestPayment(
        activity: Activity,
        resultLauncher: ActivityResultLauncher<Intent>
    ) {
        tossPayments.requestCardPayment(activity, paymentInfo, resultLauncher)
    }
}