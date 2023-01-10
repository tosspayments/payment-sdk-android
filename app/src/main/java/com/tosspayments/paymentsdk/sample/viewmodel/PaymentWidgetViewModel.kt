package com.tosspayments.paymentsdk.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

class PaymentWidgetViewModel : ViewModel() {
    private val _amount = MutableStateFlow(0L)
    val amount = _amount.asStateFlow()

    private val _orderId = MutableStateFlow("")

    private val _orderName = MutableStateFlow("")

    val uiState: StateFlow<UiState> =
        combine(_amount, _orderId, _orderName) { amount, orderId, orderName ->
            return@combine if (amount <= 0 || orderId.isBlank() || orderName.isBlank()) {
                UiState.Invalid
            } else {
                UiState.Valid(orderId, orderName)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UiState.Invalid)

    fun setAmount(amount: Long) {
        _amount.value = amount
    }

    fun setOrderId(orderId: String) {
        _orderId.value = orderId
    }

    fun setOrderName(orderName: String) {
        _orderName.value = orderName
    }

    sealed class UiState {
        object Invalid : UiState()
        data class Valid(val orderId: String, val orderName: String) : UiState()
    }
}