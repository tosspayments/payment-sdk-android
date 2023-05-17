package com.tosspayments.paymentsdk.model.paymentinfo

import org.json.JSONArray
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
            val escrowProductsPayload = JSONArray()
            escrowProducts?.map { it.json }?.forEach {
                escrowProductsPayload.put(it)
            }
            put("escrowProducts", escrowProductsPayload)
            put("useEscrow", useEscrow ?: false)

            cashReceipt?.let { put("cashReceipt", it) }

            this
        }
}
