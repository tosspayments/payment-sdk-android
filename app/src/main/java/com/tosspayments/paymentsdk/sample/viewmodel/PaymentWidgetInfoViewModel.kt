package com.tosspayments.paymentsdk.sample.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("ApplySharedPref")
class PaymentWidgetInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val _amount = MutableStateFlow(0L)
    private val _clientKey = MutableStateFlow("")
    private val _customerKey = MutableStateFlow("")
    private val _orderId = MutableStateFlow("")
    private val _orderName = MutableStateFlow("")
    private val _redirectUrl = MutableStateFlow<String?>(null)

    val paymentEnableState: StateFlow<UiState> =
        combine(
            _amount,
            _clientKey,
            _customerKey,
            _orderId,
            _orderName
        ) { amount, clientKey, customerKey, orderId, orderName ->
            val isValid = amount > 0 && arrayOf(
                clientKey,
                customerKey,
                orderId,
                orderName
            ).none { it.isBlank() }

            return@combine if (!isValid) {
                UiState.Invalid
            } else {
                UiState.Valid(amount, clientKey, customerKey, orderId, orderName, _redirectUrl.value)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UiState.Invalid)

    fun setClientKey(clientKey: String) {
        _clientKey.value = clientKey
    }

    fun setCustomerKey(customerKey: String) {
        _customerKey.value = customerKey
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

    fun setRedirectUrl(redirectUrl: String) {
        _redirectUrl.value = redirectUrl
    }

    sealed class UiState {
        object Invalid : UiState()
        data class Valid(
            val amount: Long,
            val clientKey: String,
            val customerKey: String,
            val orderId: String,
            val orderName: String,
            val redirectUrl: String?
        ) : UiState()
    }
}