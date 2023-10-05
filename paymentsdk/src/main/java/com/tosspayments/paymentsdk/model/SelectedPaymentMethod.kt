package com.tosspayments.paymentsdk.model

import org.json.JSONObject

data class SelectedPaymentMethod(
    val type: String,
    val method: String?,
    val easyPay: EasyPay?,
    val transfer: String?,
    val paymentMethodKey: String?,
) {
    companion object {
        fun fromJson(jsonObject: JSONObject): SelectedPaymentMethod {
            val type = jsonObject.getString("type")
            val method = runCatching {
                jsonObject.getString("method")
            }.getOrNull()
            val easyPay = runCatching {
                val provider = jsonObject.getJSONObject("easyPay").getString("provider")
                EasyPay(provider)
            }.getOrNull()
            val transfer = runCatching {
                jsonObject.getString("transfer")
            }.getOrNull()
            val paymentMethodKey = runCatching {
                jsonObject.getString("paymentMethodKey")
            }.getOrNull()

            return SelectedPaymentMethod(type, method, easyPay, transfer, paymentMethodKey)
        }
    }
}

data class EasyPay(
    val provider: String?,
)

data class Transfer(
    val provider: String?,
)