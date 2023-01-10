package com.tosspayments.paymentsdk.sample.model

sealed class PaymentUiState {
    object Edit : PaymentUiState()
    object Ready : PaymentUiState()
}