package com.tosspayments.paymentsdk.model

interface AgreementCallback {
    fun onAgreementStatusChanged(agreementStatus: AgreementStatus)
}