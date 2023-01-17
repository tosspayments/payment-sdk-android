package com.tosspayments.paymentsdk.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class PaymentWidgetViewModel : ViewModel() {
    private val _amount = MutableStateFlow(0L)
    val amount = _amount.asStateFlow()

    private val _orderId = MutableStateFlow("")

    private val _orderName = MutableStateFlow("")

    private val _clientKey = MutableStateFlow("")
    val clientKey = _clientKey.asStateFlow().onEach {
        saveClientKey(it)
    }

    val uiState: StateFlow<UiState> =
        combine(
            _clientKey,
            _amount,
            _orderId,
            _orderName
        ) { clientKey, amount, orderId, orderName ->
            return@combine if (clientKey.isBlank() || amount <= 0 || orderId.isBlank() || orderName.isBlank()) {
                UiState.Invalid
            } else {
                UiState.Valid(orderId, orderName)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UiState.Invalid)

    private suspend fun saveClientKey(clientKey: String) = withContext(Dispatchers.IO) {

    }

    fun setClientKey(clientKey: String) {
        _clientKey.value = clientKey
    }

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