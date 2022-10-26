package com.tosspayments.paymentsdk.model.paymentinfo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EscrowProduct(
    val id: String,
    val name: String,
    val code: String,
    val unitPrice: Long,
    val quantity: Int
) : Parcelable
