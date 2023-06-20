package com.tosspayments.paymentsdk.model

sealed class PaymentWidgetStatus {
    object Loading : PaymentWidgetStatus()
    object Loaded : PaymentWidgetStatus()
    object Failed : PaymentWidgetStatus()
}
