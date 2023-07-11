package com.tosspayments.paymentsdk.model.paymentinfo

enum class TossCardPaymentCompany(
    val code: String,
    val displayName: String,
    val appCardAvailable: Boolean
) {
    // 국내
    GWANGJUBANK("46", "광주은행", false),
    KOOKMIN("11", "KB국민카드", true),
    NONGHYEOP("91", "NH농협카드", true),
    LOTTE("71", "롯데카드", true),
    KDBBANK("30", "KDB산업은행", false),
    SAMSUNG("51", "삼성카드", true),
    SAEMAUL("38", "새마을금고", false),
    SUHYEOP("34", "Sh수협은행", false),
    SHINHAN("41", "신한카드", true),
    SHINHYEOP("62", "신협", false),
    CITI("36", "씨티카드", false),
    WOORI("33", "우리카드", false),
    POST("37", "우체국예금보험", false),
    SAVINGBANK("39", "저축은행중앙회", false),
    JEONBUKBANK("35", "전북은행", false),
    JEJUBANK("42", "제주은행", false),
    KAKAOBANK("15", "카카오뱅크", false),
    KBANK("3A", "케이뱅크", false),
    TOSSBANK("24", "토스뱅크", false),
    HANA("21", "하나카드", false),
    HYUNDAI("61", "현대카드", true),
    BC("31", "BC카드", false),
    IBK_BC("3K", "기업 BC", false),

    // 해외
    DINERS("6D", "다이너스 클럽", false),
    MASTER("4M", "마스터카드", false),
    UNIONPAY("3C", "유니온페이", false),
    AMEX("7A", "아메리칸 익스프레스", false),
    JCB("4J", "JCB", false),
    VISA("4V", "VISA", false)
}