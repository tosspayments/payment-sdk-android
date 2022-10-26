package com.tosspayments.paymentsdk.model

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.tosspayments.paymentsdk.model.paymentinfo.*

internal interface TossPayment {
    fun requestPayment(
        activity: Activity,
        method: TossPaymentMethod,
        paymentInfo: TossPaymentInfo,
        paymentResultLauncher: ActivityResultLauncher<Intent>
    )

    fun requestPayment(
        activity: Activity,
        method: TossPaymentMethod,
        paymentInfo: TossPaymentInfo,
        requestCode: Int
    )

    fun requestCardPayment(
        activity: Activity,
        paymentInfo: TossCardPaymentInfo,
        paymentResultLauncher: ActivityResultLauncher<Intent>
    )

    fun requestCardPayment(
        activity: Activity,
        paymentInfo: TossCardPaymentInfo,
        requestCode: Int
    )

    fun requestAccountPayment(
        activity: Activity,
        paymentInfo: TossAccountPaymentInfo,
        paymentResultLauncher: ActivityResultLauncher<Intent>
    )

    fun requestAccountPayment(
        activity: Activity,
        paymentInfo: TossAccountPaymentInfo,
        requestCode: Int
    )

    fun requestTransferPayment(
        activity: Activity,
        paymentInfo: TossTransferPaymentInfo,
        paymentResultLauncher: ActivityResultLauncher<Intent>
    )

    fun requestTransferPayment(
        activity: Activity,
        paymentInfo: TossTransferPaymentInfo,
        requestCode: Int
    )

    fun requestMobilePayment(
        activity: Activity,
        paymentInfo: TossMobilePaymentInfo,
        paymentResultLauncher: ActivityResultLauncher<Intent>
    )

    fun requestMobilePayment(
        activity: Activity,
        paymentInfo: TossMobilePaymentInfo,
        requestCode: Int
    )

    fun requestGiftCertificatePayment(
        activity: Activity,
        method: TossPaymentMethod.GiftCertificate,
        paymentInfo: TossPaymentInfo,
        paymentResultLauncher: ActivityResultLauncher<Intent>
    )

    fun requestGiftCertificatePayment(
        activity: Activity,
        method: TossPaymentMethod.GiftCertificate,
        paymentInfo: TossPaymentInfo,
        requestCode: Int
    )
}