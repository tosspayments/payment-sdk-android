package com.tosspayments.paymentsdk.application.activity

import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.tosspayments.paymentsdk.application.R
import com.tosspayments.paymentsdk.application.composable.Label
import com.tosspayments.paymentsdk.application.composable.OutlineButton
import com.tosspayments.paymentsdk.model.paymentinfo.TossMobilePaymentInfo
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentMobileCarrier
import com.tosspayments.paymentsdk.application.viewmodel.MobilePaymentViewModel

class MobilePaymentActivity : PaymentActivity<TossMobilePaymentInfo>() {
    override val viewModel: MobilePaymentViewModel by viewModels()

    @Composable
    override fun ExtraPaymentInfo() {
        MobileCarriers()
    }

    @Composable
    private fun MobileCarriers() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Label(text = "통신사")

            TossPaymentMobileCarrier.values().forEach {
                val isSelected =
                    viewModel.mobileCarrier.collectAsState().value?.contains(it) == true

                OutlineButton(
                    text = it.displayName,
                    color = if (isSelected) {
                        colorResource(id = R.color.light_black)
                    } else {
                        colorResource(id = R.color.gray)
                    }
                ) {
                    viewModel.selectMobileCarrier(it)
                }
            }
        }
    }
}