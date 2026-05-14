package com.tosspayments.paymentsdk.model.paymentinfo

import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

open class TossPaymentInfo(
    private val orderId: String,
    private val orderName: String,
    private val amount: Long
) {
    var customerName: String? = null
    var customerEmail: String? = null
    var taxFreeAmount: Number? = null
    var cultureExpense: Boolean = false
    var subOrders: List<SubOrder>? = null

    open val paymentPayload: (JSONObject.(JSONObject) -> (JSONObject)) = {
        this
    }

    companion object {
        private const val successUrl = "tosspayments://payment/success"
        private const val failUrl = "tosspayments://payment/fail"

        internal val successUri: Uri
            get() = Uri.parse(successUrl)
        internal val failUri: Uri
            get() = Uri.parse(failUrl)
    }

    internal fun getPayload(): JSONObject {
        val baseInfo = JSONObject().apply {
            put("amount", amount)
            put("orderId", orderId)
            put("orderName", orderName)
            put("successUrl", successUrl)
            put("failUrl", failUrl)
            put("customerName", customerName.orEmpty())
            put("customerEmail", customerEmail.orEmpty())
            put("taxFreeAmount", taxFreeAmount)
            put("cultureExpense", cultureExpense)
            subOrders?.let {
                put("subOrders", JSONArray().apply {
                    it.map { subOrder -> subOrder.json }.forEach { json ->
                        put(json)
                    }
                })
            }
        }

        return paymentPayload.invoke(baseInfo, baseInfo)
    }
}
