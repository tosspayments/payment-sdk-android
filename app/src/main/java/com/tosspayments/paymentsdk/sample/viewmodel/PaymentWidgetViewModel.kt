package com.tosspayments.paymentsdk.sample.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("ApplySharedPref")
class PaymentWidgetViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TEST_CLIENT_KEY = "test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq"
        private const val DEFAULT_ORDER_ID = "AD8aZDpbzXs4EQa"
    }

    private val _amount = MutableStateFlow(0L)
    val amount = _amount.asStateFlow()

    private val _orderId = MutableStateFlow(DEFAULT_ORDER_ID)
    val orderId = _orderId.asStateFlow().onEach {
        saveDefaultValue(orderIdPref, it)
    }.shareIn(viewModelScope, replay = 1, started = SharingStarted.WhileSubscribed())

    private val _orderIdList = MutableStateFlow(emptyList<String>())
    val orderIdList = _orderIdList.asStateFlow()

    private val _orderName = MutableStateFlow("")

    private val _clientKey = MutableStateFlow(TEST_CLIENT_KEY)
    val clientKey = _clientKey.asStateFlow().onEach {
        saveDefaultValue(clientKeyPref, it)
    }.shareIn(viewModelScope, replay = 1, started = SharingStarted.WhileSubscribed())

    private val _clientKeyList = MutableStateFlow(emptyList<String>())
    val clientKeyList = _clientKeyList.asStateFlow()

    private val clientKeyPref =
        application.getSharedPreferences("clientKeyPref", Context.MODE_PRIVATE)

    private val orderIdPref =
        application.getSharedPreferences("orderIdPref", Context.MODE_PRIVATE)

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

    init {
        initClientKeyPref()
        initOrderIdPref()
    }

    private fun initClientKeyPref() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!clientKeyPref.contains(TEST_CLIENT_KEY)) {
                clientKeyPref.edit().putBoolean(TEST_CLIENT_KEY, false).commit()
            }

            val entry = clientKeyPref.all
            entry.forEach {
                if (it.value == true) {
                    _clientKey.emit(it.key)
                    return@forEach
                }
            }

            _clientKeyList.emit(entry.map { it.key })
        }
    }

    private fun initOrderIdPref() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!orderIdPref.contains(DEFAULT_ORDER_ID)) {
                orderIdPref.edit().putBoolean(DEFAULT_ORDER_ID, true).commit()
            }

            val entry = orderIdPref.all
            entry.forEach {
                if (it.value == true) {
                    _orderId.emit(it.key)
                    return@forEach
                }
            }

            _orderIdList.emit(entry.map { it.key })
        }
    }

    private suspend fun saveDefaultValue(pref: SharedPreferences, defaultValue: String) =
        withContext(Dispatchers.IO) {
            if (defaultValue.trim().isNotBlank()) {
                val entry = pref.all

                entry.keys.forEach { key ->
                    pref.edit().putBoolean(key, defaultValue == key).apply()
                }

                pref.edit().putBoolean(defaultValue, true).apply()
            }
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