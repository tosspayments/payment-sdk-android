package com.tosspayments.paymentsdk.model

interface AgreementStatusListener {
    fun onAgreementStatusChanged(agreementStatus: AgreementStatus)
}