package com.tosspayments.paymentsdk.application.activity

import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.tosspayments.paymentsdk.application.composable.ItemSelectDialog
import com.tosspayments.paymentsdk.application.composable.PaymentInfoInput
import com.tosspayments.paymentsdk.model.paymentinfo.TossCardPaymentCompany
import com.tosspayments.paymentsdk.model.paymentinfo.TossCardPaymentFlow
import com.tosspayments.paymentsdk.model.paymentinfo.TossCardPaymentInfo
import com.tosspayments.paymentsdk.model.paymentinfo.TossEasyPayCompany
import com.tosspayments.paymentsdk.application.viewmodel.CardPaymentViewModel

class CardPaymentActivity : PaymentActivity<TossCardPaymentInfo>() {
    override val viewModel: CardPaymentViewModel by viewModels()

    @Composable
    override fun ExtraPaymentInfo() {
        CardCompany()
        InstallmentPlan()
        MaxInstallmentPlan()
        UseCardPoint()
        UseAppCardOnly()
        UseInternationalCardOnly()
        FlowMode()
        EasyPay()
        DiscountCode()
    }

    @Composable
    private fun CardCompany() {
        ItemSelectDialog("카드사",
            buttonText = viewModel.cardCompany.collectAsState().value?.displayName ?: "카드사 선택",
            items = TossCardPaymentCompany.values().map { Pair(it.displayName, it) }) {
            viewModel.setCardCompany(it)
        }
    }

    @Composable
    private fun InstallmentPlan() {
        if (kotlin.runCatching {
                viewModel.amount.collectAsState().value.toLong() > 50000
            }.getOrDefault(false)) {
            val selectedInstallmentMonth = viewModel.installmentPlan.collectAsState().value

            ItemSelectDialog(
                label = "할부개월",
                buttonText = when (selectedInstallmentMonth) {
                    null -> "할부개월 선택"
                    0, 1 -> "일시불"
                    else -> {
                        "${selectedInstallmentMonth}개월"
                    }
                },
                items = listOf(Pair("미설정", null), Pair("일시불", 0)) + (2..12).map {
                    Pair("${it}개월", it)
                }
            ) {
                viewModel.setInstallmentPlan(it)
            }
        }
    }

    @Composable
    private fun MaxInstallmentPlan() {
        if (kotlin.runCatching {
                viewModel.amount.collectAsState().value.toLong() > 50000
            }.getOrDefault(false)) {
            val selectedInstallmentMonth = viewModel.maxInstallmentPlan.collectAsState().value

            ItemSelectDialog(
                label = "최대 할부개월",
                buttonText = when (selectedInstallmentMonth) {
                    null -> "할부개월 선택"
                    else -> {
                        "${selectedInstallmentMonth}개월"
                    }
                },
                items = listOf(Pair("미설정", null)) + (2..12).map {
                    Pair("${it}개월", it)
                }
            ) {
                viewModel.setMaxInstallmentPlan(it)
            }
        }
    }

    @Composable
    private fun UseCardPoint() {
        val useCardPoint = viewModel.useCardPoint.collectAsState().value

        ItemSelectDialog(
            label = "카드포인트",
            buttonText = when (useCardPoint) {
                null -> "미설정"
                true -> "사용"
                else -> "사용불가"
            },
            items = listOf(Pair("미설정", null), Pair("사용", true), Pair("사용불가", false))
        ) {
            viewModel.setUseCardPoint(it)
        }
    }

    @Composable
    private fun UseAppCardOnly() {
        if (viewModel.cardCompany.collectAsState().value?.appCardAvailable == true) {
            val useAppCardOnly = viewModel.useAppCardOnly.collectAsState().value

            ItemSelectDialog(
                label = "앱카드로 결제",
                buttonText = when (useAppCardOnly) {
                    true -> "사용"
                    else -> "미사용"
                },
                items = listOf(Pair("사용", true), Pair("미사용", null))
            ) {
                viewModel.setUseAppCardOnly(it)
            }
        }
    }

    @Composable
    private fun UseInternationalCardOnly() {
        val useInternationalCardOnly = viewModel.useInternationalCardOnly.collectAsState().value

        ItemSelectDialog(
            label = "해외카드 결제",
            buttonText = when (useInternationalCardOnly) {
                true -> "사용"
                else -> "미사용"
            },
            items = listOf(Pair("사용", true), Pair("미사용", null))
        ) {
            viewModel.setUseInternationalCardOnly(it)
        }
    }

    @Composable
    private fun FlowMode() {
        if (viewModel.cardCompany.collectAsState().value != null) {
            val flowMode = viewModel.flowMode.collectAsState().value

            ItemSelectDialog(
                label = "카드사 결제로 직접 연결",
                buttonText = when (flowMode) {
                    TossCardPaymentFlow.DIRECT -> "연결"
                    else -> "미연결"
                },
                items = listOf(
                    Pair("연결", TossCardPaymentFlow.DIRECT),
                    Pair("미연결", TossCardPaymentFlow.DEFAULT)
                )
            ) {
                viewModel.setFlowMode(it ?: TossCardPaymentFlow.DEFAULT)
            }
        }
    }

    @Composable
    private fun EasyPay() {
        if (viewModel.flowMode.collectAsState().value == TossCardPaymentFlow.DIRECT) {
            val easyPay = viewModel.easyPay.collectAsState().value

            ItemSelectDialog(
                label = "간편결제",
                buttonText = when (easyPay) {
                    null -> "간편결제 선택"
                    else -> easyPay.displayName
                },
                items = TossEasyPayCompany.values().map {
                    Pair(it.displayName, it)
                }
            ) {
                viewModel.setEasyPay(it)
            }
        }
    }

    @Composable
    private fun DiscountCode() {
        if (viewModel.flowMode.collectAsState().value == TossCardPaymentFlow.DIRECT) {
            PaymentInfoInput(
                labelText = "DiscountCode",
                initInputText = viewModel.discountCode.collectAsState().value.orEmpty()
            ) {
                viewModel.setDiscountCode(it)
            }
        }
    }
}