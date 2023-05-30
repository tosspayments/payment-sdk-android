package com.tosspayments.paymentsdk.model.paymentinfo

import android.net.Uri
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

    open val paymentPayload: (JSONObject.(JSONObject) -> (JSONObject)) = {
        this
    }

    companion object {
        internal val successUri = Uri.parse("tosspayments://payment/success")
        internal val failUri = Uri.parse("tosspayments://payment/fail")
    }

    internal fun getPayload(): JSONObject {
        val baseInfo = JSONObject().apply {
            put("amount", amount)
            put("orderId", orderId)
            put("orderName", orderName)
            put("successUrl", successUri)
            put("failUrl", failUri)
            put("customerName", customerName.orEmpty())
            put("customerEmail", customerEmail.orEmpty())
            put("taxFreeAmount", taxFreeAmount)
            put("cultureExpense", cultureExpense)
        }

        return paymentPayload.invoke(baseInfo, baseInfo)
    }
}