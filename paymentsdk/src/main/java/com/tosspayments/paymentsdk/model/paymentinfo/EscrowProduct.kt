package com.tosspayments.paymentsdk.model.paymentinfo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class EscrowProduct(
    val id: String,
    val name: String,
    val code: String,
    val unitPrice: Long,
    val quantity: Int
) : Parcelable {
    val json: JSONObject
        get() = JSONObject()
            .put("id", id)
            .put("name", name)
            .put("code", code)
            .put("unitPrice", unitPrice)
            .put("quantity", quantity)
}
