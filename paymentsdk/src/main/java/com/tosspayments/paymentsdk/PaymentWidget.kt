package com.tosspayments.paymentsdk

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.tosspayments.paymentsdk.interfaces.PaymentWidgetCallback
import com.tosspayments.paymentsdk.view.PaymentMethodWidget

class PaymentWidget(private val clientKey: String) {
    private var tossPayments: TossPayments = TossPayments(clientKey)

    private var methodWidget: PaymentMethodWidget? = null
    private var requestCode: Int? = null
    private var paymentResultLauncher: ActivityResultLauncher<Intent>? = null

    private fun getPaymentWidgetCallback(orderId: String): PaymentWidgetCallback {
        return object : PaymentWidgetCallback {
            override fun onPaymentDomCreated(paymentDom: String) {
                methodWidget?.context?.let {
                    if (paymentDom.isNotBlank()) {
                        when {
                            requestCode != null -> {
                                tossPayments.requestPayment(
                                    it,
                                    paymentDom,
                                    orderId,
                                    requestCode!!
                                )
                            }
                            paymentResultLauncher != null -> {
                                tossPayments.requestPayment(
                                    it,
                                    paymentDom,
                                    orderId,
                                    paymentResultLauncher!!
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun setMethodWidget(methodWidget: PaymentMethodWidget) {
        this.methodWidget = methodWidget
    }

    fun renderPaymentMethodWidget(customerKey: String, amount: Long) {
        methodWidget?.renderPaymentMethods(clientKey, customerKey, amount)
    }

    @JvmOverloads
    @Throws(IllegalAccessException::class)
    fun requestPayment(
        paymentResultLauncher: ActivityResultLauncher<Intent>,
        orderId: String,
        orderName: String,
        customerEmail: String? = null,
        customerName: String? = null
    ) {
        methodWidget?.let {
            this.paymentResultLauncher = paymentResultLauncher

            it.requestPayment(
                orderId,
                orderName,
                customerEmail,
                customerName,
                getPaymentWidgetCallback(orderId)
            )
        } ?: kotlin.run {
            this.paymentResultLauncher = null
        }
    }

    @JvmOverloads
    @Throws(IllegalAccessException::class)
    fun requestPayment(
        requestCode: Int,
        orderId: String,
        orderName: String,
        customerEmail: String? = null,
        customerName: String? = null,
    ) {
        methodWidget?.let {
            this.requestCode = requestCode

            it.requestPayment(
                orderId,
                orderName,
                customerEmail,
                customerName,
                getPaymentWidgetCallback(orderId)
            )
        } ?: kotlin.run {
            this.requestCode = null
        }
    }
}