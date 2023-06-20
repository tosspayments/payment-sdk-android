package com.tosspayments.paymentsdk.model

interface PaymentWidgetStatusListener {
    fun onLoading()
    fun onLoaded()
    fun onFailed()
}