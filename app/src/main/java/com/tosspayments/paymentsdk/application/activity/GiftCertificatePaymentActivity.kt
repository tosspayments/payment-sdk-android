package com.tosspayments.paymentsdk.application.activity

import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.tosspayments.paymentsdk.application.composable.ItemSelectDialog
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentMethod
import com.tosspayments.paymentsdk.application.viewmodel.GiftCertificatePaymentViewModel

class GiftCertificatePaymentActivity : PaymentActivity<TossPaymentInfo>() {
    override val viewModel: GiftCertificatePaymentViewModel by viewModels()

    @Composable
    override fun ExtraPaymentInfo() {
        GiftCard()
    }

    @Composable
    private fun GiftCard() {
        ItemSelectDialog(
            label = "상품권",
            buttonText = viewModel.method.collectAsState().value.displayName,
            items = listOf(
                Pair(
                    TossPaymentMethod.GiftCertificate.Culture.displayName,
                    TossPaymentMethod.GiftCertificate.Culture
                ),
                Pair(
                    TossPaymentMethod.GiftCertificate.Book.displayName,
                    TossPaymentMethod.GiftCertificate.Book
                ),
                Pair(
                    TossPaymentMethod.GiftCertificate.Game.displayName,
                    TossPaymentMethod.GiftCertificate.Game
                )
            )
        ) {
            it?.let { giftCard ->
                viewModel.setMethod(giftCard)
            }
        }
    }
}