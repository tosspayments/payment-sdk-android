package com.tosspayments.paymentsdk.model.paymentinfo

import org.json.JSONArray
import org.json.JSONObject

data class TossMobilePaymentInfo(
    val orderId: String,
    val orderName: String,
    val amount: Long
) : TossPaymentInfo(orderId, orderName, amount) {
    var mobileCarrierList: List<TossPaymentMobileCarrier>? = null

    override val paymentPayload: JSONObject.(JSONObject) -> JSONObject
        get() = {
            mobileCarrierList?.let {
                put("mobileCarrier", JSONArray().apply {
                    it.forEach { code ->
                        this.put(code)
                    }
                })
            } ?: this
        }
}