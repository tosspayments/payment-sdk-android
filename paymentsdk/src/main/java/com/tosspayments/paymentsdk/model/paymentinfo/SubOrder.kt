package com.tosspayments.paymentsdk.model.paymentinfo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class MerchantAddress(
    val country: String,
    val postalCode: String,
    val address: String,
    val detailAddress: String? = null
) : Parcelable {
    val json: JSONObject
        get() = JSONObject()
            .put("country", country)
            .put("postalCode", postalCode)
            .put("address", address)
            .apply {
                detailAddress?.let { put("detailAddress", it) }
            }
}

@Parcelize
data class SubOrder(
    val merchantBusinessNumber: String,
    val merchantName: String,
    val merchantAddress: MerchantAddress,
    val orderName: String
) : Parcelable {
    val json: JSONObject
        get() = JSONObject()
            .put("merchantBusinessNumber", merchantBusinessNumber)
            .put("merchantName", merchantName)
            .put("merchantAddress", merchantAddress.json)
            .put("orderName", orderName)
}
