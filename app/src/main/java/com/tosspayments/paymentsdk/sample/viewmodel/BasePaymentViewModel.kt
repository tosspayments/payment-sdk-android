package com.tosspayments.paymentsdk.sample.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tosspayments.paymentsdk.TossPayments
import com.tosspayments.paymentsdk.model.paymentinfo.MerchantAddress
import com.tosspayments.paymentsdk.model.paymentinfo.SubOrder
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo
import com.tosspayments.paymentsdk.sample.model.PaymentUiState
import kotlinx.coroutines.flow.*

abstract class BasePaymentViewModel<T : TossPaymentInfo> : ViewModel() {
    companion object {
        private const val DEFAULT_CLIENT_KEY = "test_ck_D4yKeq5bgrplRRJjKGArGX0lzW6Y"
        private const val DEFAULT_AMOUNT = 60000L
        private const val DEFAULT_ORDER_ID = "dnA8Bcq46FEpCjg08ZFMf"
        private const val DEFAULT_ORDER_NAME = "Toss 후드티"
    }

    protected abstract val paymentInfo: T

    protected val tossPayments: TossPayments
        get() = TossPayments(_clientKey.value)

    private val _clientKey = MutableStateFlow(DEFAULT_CLIENT_KEY)
    val clientKey = _clientKey.asStateFlow()

    protected val _amount = MutableStateFlow(DEFAULT_AMOUNT)
    val amount = _amount.asStateFlow()

    protected val _orderId = MutableStateFlow(DEFAULT_ORDER_ID)
    val orderId = _orderId.asStateFlow()

    protected val _orderName = MutableStateFlow(DEFAULT_ORDER_NAME)
    val orderName = _orderName.asStateFlow()

    protected val _customerName = MutableStateFlow("")
    val customerName = _customerName.asStateFlow()

    protected val _customerEmail = MutableStateFlow("")
    val customerEmail = _customerEmail.asStateFlow()

    protected val _taxFreeAmount = MutableStateFlow(0L)
    val taxFreeAmount = _taxFreeAmount.asStateFlow()

    protected val _subOrders = MutableStateFlow<List<SubOrder>>(emptyList())
    val subOrders = _subOrders.asStateFlow()

    val uiState =
        combine(_orderId, _orderName, _amount) { orderId, orderName, amount ->
            return@combine if (orderId.isNotBlank() && orderName.isNotBlank() && amount > 0) PaymentUiState.Ready else PaymentUiState.Edit
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun setClientKey(clientKey: String) {
        _clientKey.value = clientKey
    }

    fun setAmount(amount: String?) {
        _amount.value = if (amount.isNullOrBlank()) 0L else amount.toLong()
    }

    fun setOrderId(orderId: String) {
        _orderId.value = orderId
    }

    fun setOrderName(orderName: String) {
        _orderName.value = orderName
    }

    fun setCustomerName(customerName: String) {
        _customerName.value = customerName
    }

    fun setCustomerEmail(customerEmail: String) {
        _customerEmail.value = customerEmail
    }

    fun setTaxFreeAmount(taxFreeAmount: String?) {
        _taxFreeAmount.value = if (taxFreeAmount.isNullOrBlank()) 0L else taxFreeAmount.toLong()
    }

    fun addSubOrder() {
        _subOrders.value = _subOrders.value + emptySubOrder()
    }

    fun updateSubOrder(index: Int, subOrder: SubOrder) {
        _subOrders.value = _subOrders.value.mapIndexed { currentIndex, currentSubOrder ->
            if (currentIndex == index) subOrder else currentSubOrder
        }
    }

    fun removeSubOrder(index: Int) {
        _subOrders.value = _subOrders.value.filterIndexed { currentIndex, _ ->
            currentIndex != index
        }
    }

    protected fun normalizedSubOrders(): List<SubOrder>? {
        return _subOrders.value.filter { it.isComplete() }.takeIf { it.isNotEmpty() }
    }

    private fun emptySubOrder(): SubOrder {
        return SubOrder(
            merchantBusinessNumber = "",
            merchantName = "",
            merchantAddress = MerchantAddress(
                country = "",
                postalCode = "",
                address = "",
                detailAddress = null
            ),
            orderName = ""
        )
    }

    private fun SubOrder.isComplete(): Boolean {
        return merchantBusinessNumber.isNotBlank() &&
            merchantName.isNotBlank() &&
            merchantAddress.country.isNotBlank() &&
            merchantAddress.postalCode.isNotBlank() &&
            merchantAddress.address.isNotBlank() &&
            orderName.isNotBlank()
    }

    abstract fun requestPayment(activity: Activity, resultLauncher: ActivityResultLauncher<Intent>)
}
