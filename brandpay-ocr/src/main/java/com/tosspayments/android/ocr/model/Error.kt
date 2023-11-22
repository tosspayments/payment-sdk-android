package com.tosspayments.android.ocr.model

import com.google.gson.annotations.SerializedName

class OcrError(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String
)