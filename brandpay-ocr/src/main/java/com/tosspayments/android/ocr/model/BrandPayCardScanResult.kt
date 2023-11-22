package com.tosspayments.android.ocr.model

import com.google.gson.annotations.SerializedName

data class BrandPayCardScanResult(
    @SerializedName("cardNo1")
    val cardNo1: String? = "",
    @SerializedName("cardNo2")
    val cardNo2: String? = "",
    @SerializedName("cardNo3")
    val cardNo3: String? = "",
    @SerializedName("cardNo4")
    val cardNo4: String? = "",
    @SerializedName("expiryDate")
    val expiryDate: String? = ""
)
