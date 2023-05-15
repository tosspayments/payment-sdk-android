package com.tosspayments.paymentsdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.tosspayments.paymentsdk.activity.TossPaymentsWebActivity
import com.tosspayments.paymentsdk.interfaces.PaymentWidgetJavascriptInterface
import com.tosspayments.paymentsdk.model.Constants
import com.tosspayments.paymentsdk.model.PaymentWidgetOptions
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.view.PaymentMethodWidget

class PaymentWidget(
    activity: AppCompatActivity,
    private val clientKey: String,
    private val customerKey: String,
    options: PaymentWidgetOptions? = null
) {
    private val tossPayments: TossPayments = TossPayments(clientKey)

    private val redirectUrl = options?.brandPayOption?.redirectUrl
    private val domain = try {
        Uri.parse(redirectUrl).let {
            "${it.authority}${it.host}"
        }
    } catch (e: Exception) {
        null
    }

    private var methodWidget: PaymentMethodWidget? = null
    private var requestCode: Int? = null
    private var paymentResultLauncher: ActivityResultLauncher<Intent>? = null

    private val htmlRequestActivityResult =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                methodWidget?.evaluateJavascript(
                    result.data?.getStringExtra(Constants.EXTRA_KEY_DATA).orEmpty()
                )
            }
        }

    private var orderId: String = ""

    companion object {
        @JvmStatic
        fun getPaymentResultLauncher(
            activity: AppCompatActivity,
            onSuccess: (TossPaymentResult.Success) -> Unit,
            onFailed: (TossPaymentResult.Fail) -> Unit
        ): ActivityResultLauncher<Intent> {
            return TossPayments.getPaymentResultLauncher(activity, onSuccess, onFailed)
        }
    }

    private fun handlePaymentDom(context: Context, paymentHtml: String) {
        if (paymentHtml.isNotBlank()) {
            when {
                requestCode != null -> {
                    tossPayments.requestPayment(
                        context = context,
                        paymentHtml = paymentHtml,
                        orderId = orderId,
                        requestCode = requestCode!!,
                        domain = domain
                    )
                }
                paymentResultLauncher != null -> {
                    tossPayments.requestPayment(
                        context = context,
                        paymentHtml = paymentHtml,
                        orderId = orderId,
                        paymentResultLauncher = paymentResultLauncher!!,
                        domain = domain
                    )
                }
            }
        }
    }

    @Deprecated("This function is no longer needed", level = DeprecationLevel.WARNING)
    fun setMethodWidget(methodWidget: PaymentMethodWidget) {
        this.methodWidget = methodWidget
    }

    /**
     * This function has been deprecated because it is no longer needed. Use [renderPaymentMethods] instead.
     */
    @Deprecated(
        "This function is no longer needed. Use renderPaymentMethods instead.",
        replaceWith = ReplaceWith("renderPaymentMethods()"),
        level = DeprecationLevel.WARNING
    )
    fun renderPaymentMethodWidget(amount: Number, orderId: String) {
        methodWidget?.renderPaymentMethods(
            clientKey,
            customerKey,
            amount,
            redirectUrl
        )
    }

    fun renderPaymentMethods(methodWidget: PaymentMethodWidget, amount: Number) {
        this.methodWidget = methodWidget.apply {
            addJavascriptInterface(object : PaymentWidgetJavascriptInterface(this) {
                @JavascriptInterface
                fun requestPayments(html: String) {
                    handlePaymentDom(context, html)
                }

                @JavascriptInterface
                fun requestHTML(html: String) {
                    htmlRequestActivityResult.launch(
                        TossPaymentsWebActivity.getIntent(context, domain, html)
                    )
                }
            })
        }

        methodWidget.renderPaymentMethods(
            clientKey,
            customerKey,
            amount,
            redirectUrl
        )
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
            this.orderId = orderId

            it.requestPayment(
                orderId,
                orderName,
                customerEmail,
                customerName,
                redirectUrl
            )
        } ?: kotlin.run {
            this.paymentResultLauncher = null
            this.orderId = ""
            throw IllegalAccessException("Payment method widget is not rendered.")
        }
    }

    @JvmOverloads
    @Throws(IllegalAccessException::class)
    fun requestPayment(
        requestCode: Int,
        orderId: String,
        orderName: String,
        customerEmail: String? = null,
        customerName: String? = null
    ) {
        methodWidget?.let {
            this.requestCode = requestCode

            it.requestPayment(
                orderId,
                orderName,
                customerEmail,
                customerName,
                redirectUrl
            )
        } ?: kotlin.run {
            this.requestCode = null
            throw IllegalAccessException("Payment method widget is not set")
        }
    }
}