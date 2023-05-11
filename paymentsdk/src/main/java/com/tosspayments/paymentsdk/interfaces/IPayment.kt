package com.tosspayments.paymentsdk.interfaces

internal interface IPayment {
    fun requestPayment(
        orderId: String,
        orderName: String,
        customerEmail: String? = null,
        customerName: String? = null,
        redirectUrl: String? = null
    )
}