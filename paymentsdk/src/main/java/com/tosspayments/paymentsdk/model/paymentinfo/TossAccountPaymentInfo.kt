package com.tosspayments.paymentsdk.model.paymentinfo

import org.json.JSONObject

data class TossAccountPaymentInfo(
    val orderId: String,
    val orderName: String,
    val amount: Long
) : TossPaymentInfo(orderId, orderName, amount) {
    var validHours: Int? = null
    var dueDate: String? = null
    var customerMobilePhone: String? = null
    var showCustomerMobilePhone: Boolean? = null
    var cashReceipt: String? = null
    var useEscrow: Boolean? = null
    var escrowProducts: List<EscrowProduct>? = null
    var currency: String = "KRW"

    override val paymentPayload: JSONObject.(JSONObject) -> JSONObject
        get() = {
            validHours?.let { put("validHours", it) }
            dueDate?.let { put("dueDate", it) }
            customerMobilePhone?.let { put("customerMobilePhone", it) }
            showCustomerMobilePhone?.let { put("showCustomerMobilePhone", it) }
            cashReceipt?.let { put("cashReceipt", it) }
            useEscrow?.let { put("useEscrow", it) }
            escrowProducts?.let { put("escrowProducts", it) }

            put("currency", currency)
        }
}