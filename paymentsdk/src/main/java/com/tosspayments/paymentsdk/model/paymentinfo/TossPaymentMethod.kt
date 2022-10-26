package com.tosspayments.paymentsdk.model.paymentinfo

sealed class TossPaymentMethod(val displayName: String) {
    object Card : TossPaymentMethod("카드")
    object TossPay : TossPaymentMethod("토스페이")
    object Account : TossPaymentMethod("가상계좌")
    object Transfer : TossPaymentMethod("계좌이체")
    object Mobile : TossPaymentMethod("휴대폰")

    sealed class GiftCertificate(var name: String) : TossPaymentMethod(displayName = name) {
        object Culture : GiftCertificate("문화상품권")
        object Book : GiftCertificate("도서문화상품권")
        object Game : GiftCertificate("게임문화상품권")
    }
}