package com.tosspayments.paymentsdk.application.model

sealed class PaymentUiState {
    object Edit : PaymentUiState()
    object Ready : PaymentUiState()
}