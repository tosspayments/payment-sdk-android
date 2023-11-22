package com.tosspayments.android.ocr.model

enum class ErrorCode(val message: String) {
    OCR_CANCELED("카드스캔 취소."),
    OCR_INVALID("카드 스캔 실행을 위해서 카메라 권한이 필요합니다."),
    OCR_FAILED("카드스캔 실패.")
}
