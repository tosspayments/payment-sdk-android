package com.tosspayments.paymentsdk.model.paymentinfo

import org.json.JSONObject

data class TossTransferPaymentInfo(
    val orderId: String,
    val orderName: String,
    val amount: Long
) : TossPaymentInfo(orderId, orderName, amount) {
    var cashReceipt: String? = null
    var useEscrow: Boolean? = null
    var escrowProducts: List<EscrowProduct>? = null

    override val paymentPayload: JSONObject.(JSONObject) -> JSONObject
        get() = {
            cashReceipt?.let { put("cashReceipt", it) }
            useEscrow?.let { put("useEscrow", it) }
            escrowProducts?.let { put("escrowProducts", it) }

            this
        }
}
