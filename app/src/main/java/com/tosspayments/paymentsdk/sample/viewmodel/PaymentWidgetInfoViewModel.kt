package com.tosspayments.paymentsdk.sample.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tosspayments.paymentsdk.view.PaymentMethod
import kotlinx.coroutines.flow.*

@SuppressLint("ApplySharedPref")
class PaymentWidgetInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val _amount = MutableStateFlow<Number>(0.0)
    private val _clientKey = MutableStateFlow("")
    private val _customerKey = MutableStateFlow("")
    private val _orderId = MutableStateFlow("")
    private val _orderName = MutableStateFlow("")
    private val _redirectUrl = MutableStateFlow<String?>(null)

    private val _currency = MutableLiveData(PaymentMethod.Rendering.Currency.KRW)
    val currency: LiveData<PaymentMethod.Rendering.Currency> = _currency

    var countryCode = ""
    var variantKey: String? = null

    val paymentEnableState: StateFlow<UiState> =
        combine(
            _amount,
            _clientKey,
            _customerKey,
            _orderId,
            _orderName
        ) { amount, clientKey, customerKey, orderId, orderName ->
            val isValid = amount.toDouble() > 0 && arrayOf(
                clientKey,
                customerKey,
                orderId,
                orderName
            ).none { it.isBlank() }

            return@combine if (!isValid) {
                UiState.Invalid
            } else {
                UiState.Valid(
                    amount = amount,
                    clientKey = clientKey,
                    customerKey = customerKey,
                    orderId = orderId,
                    orderName = orderName,
                    redirectUrl = _redirectUrl.value
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UiState.Invalid)

    fun setClientKey(clientKey: String) {
        _clientKey.value = clientKey
    }

    fun setCustomerKey(customerKey: String) {
        _customerKey.value = customerKey
    }

    fun setAmount(amount: Number) {
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

    fun setCurrency(currency: PaymentMethod.Rendering.Currency) {
        _currency.value = currency
    }

    sealed class UiState {
        object Invalid : UiState()
        data class Valid(
            val amount: Number,
            val clientKey: String,
            val customerKey: String,
            val orderId: String,
            val orderName: String,
            val redirectUrl: String? = null
        ) : UiState()
    }
}