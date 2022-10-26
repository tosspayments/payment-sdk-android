package com.tosspayments.paymentsdk.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


sealed interface TossPaymentResult {
    @Parcelize
    class Success(val paymentKey: String, val orderId: String, val amount: Long) :
        TossPaymentResult, Parcelable

    @Parcelize
    class Fail(val errorCode: String, val errorMessage: String, val orderId: String) :
        TossPaymentResult, Parcelable
}