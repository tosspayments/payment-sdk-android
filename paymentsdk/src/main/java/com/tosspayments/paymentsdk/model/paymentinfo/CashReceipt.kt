package com.tosspayments.paymentsdk.model.paymentinfo

enum class CashReceipt(
    val value: String
) {
    DEDUCTION("소득공제"), PAYMENT_PROVE("지출증빙"), INVALID("미발행")
}
