package com.tosspayments.paymentsdk.model

interface PaymentCallback {
    fun onPaymentSuccess(success: TossPaymentResult.Success)
    fun onPaymentFailed(fail: TossPaymentResult.Fail)
}