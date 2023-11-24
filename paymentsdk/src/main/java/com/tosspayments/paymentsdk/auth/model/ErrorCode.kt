package com.tosspayments.paymentsdk.auth.model

enum class ErrorCode(val message: String) {
    BIOMETRIC_CANCELED("생체인증 취소."), BIOMETRIC_INVALID("생체인증 유효하지 않음."), BIOMETRIC_FAILED("생체인증 실패.")
}
