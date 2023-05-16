package com.tosspayments.paymentsdk.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet

@SuppressLint("SetJavaScriptEnabled")
class Agreement(context: Context, attrs: AttributeSet? = null) :
    PaymentWidgetContainer(context, attrs) {

    companion object {
        internal const val EVENT_NAME_UPDATE_AGREEMENT_STATUS = "updateAgreementStatus"
    }

    internal fun renderAgreement(
        clientKey: String,
        customerKey: String,
        domain: String? = null
    ) {
        renderWidget(clientKey, customerKey, domain) {
            appendLine("const paymentAgreement = paymentWidget.renderAgreement('#agreement');")
        }
    }
}