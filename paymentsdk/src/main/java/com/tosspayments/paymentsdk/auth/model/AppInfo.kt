package com.tosspayments.paymentsdk.auth.model

import com.google.gson.annotations.SerializedName
import com.tosspayments.paymentsdk.BuildConfig

data class AppInfo(
    @SerializedName("app_id")
    val app_id: String,
    @SerializedName("sdk_version")
    val sdkVersion: String = BuildConfig.MODULE_VERSION_NAME,
    @SerializedName("os")
    val os: String = "android"
)
