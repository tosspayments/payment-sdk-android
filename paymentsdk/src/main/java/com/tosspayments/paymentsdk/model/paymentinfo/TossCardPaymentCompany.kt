package com.tosspayments.paymentsdk.model.paymentinfo

enum class TossCardPaymentCompany(val displayName: String, val appCardAvailable : Boolean) {
    GWANGJUBANK("광주", false),
    KOOKMIN("국민", true),
    NONGHYEOP("농협", true),
    LOTTE("롯데", true),
    KDBBANK("산업", false),
    SAMSUNG("삼성", true),
    SAEMAUL("새마을", false),
    SUHYEOP("수협", false),
    SHINHAN("신한", true),
    SHINHYEOP("신협", false),
    CITI("씨티", false),
    WOORI("우리", false),
    POST("우체국", false),
    SAVINGBANK("저축", false),
    JEONBUKBANK("전북", false),
    JEJUBANK("제주", false),
    KAKAOBANK("카카오뱅크", false),
    KBANK("케이뱅크", false),
    TOSSBANK("토스뱅크", false),
    HANA("하나", false),
    HYUNDAI("현대", true),
    BC("비씨", false)
}