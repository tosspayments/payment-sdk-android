package com.tosspayments.paymentsdk.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet

@SuppressLint("SetJavaScriptEnabled")
class Agreement(context: Context, attrs: AttributeSet? = null) :
    PaymentWidgetContainer(context, attrs) {
    override val widgetName: String
        get() = "agreement"

    companion object {
        internal const val EVENT_NAME_UPDATE_AGREEMENT_STATUS = "updateAgreementStatus"

        internal const val MESSAGE_NOT_RENDERED =
            "PaymentMethod is not rendered. Call 'renderPaymentMethods' method first."
    }

    internal fun renderAgreement(
        clientKey: String,
        customerKey: String
    ) {
        renderWidget(clientKey, customerKey) {
            appendLine("const paymentAgreement = paymentWidget.renderAgreement('#agreement');")
        }
    }
}