package com.tosspayments.paymentsdk.interfaces

import com.tosspayments.paymentsdk.model.TossPaymentResult

interface TossPaymentCallback {
    fun onSuccess(success: TossPaymentResult.Success)
    fun onFailed(fail: TossPaymentResult.Fail)
    fun onSuccess(html : String)
}