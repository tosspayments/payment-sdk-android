package com.tosspayments.paymentsdk.model

interface PaymentWidgetStatusListener {
    fun onLoad()

    fun onFail(fail: TossPaymentResult.Fail)
}