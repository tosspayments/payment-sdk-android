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
import com.tosspayments.paymentsdk.model.*
import com.tosspayments.paymentsdk.view.Agreement
import com.tosspayments.paymentsdk.view.PaymentMethod
import com.tosspayments.paymentsdk.view.PaymentWidgetContainer
import org.json.JSONObject

class PaymentWidget(
    activity: AppCompatActivity,
    private val clientKey: String,
    private val customerKey: String,
    paymentOptions: PaymentWidgetOptions? = null
) {
    private val tossPayments: TossPayments = TossPayments(clientKey)

    private val redirectUrl = paymentOptions?.brandPayOption?.redirectUrl
    private val domain = try {
        if (!redirectUrl.isNullOrBlank()) {
            Uri.parse(redirectUrl).let {
                "${it.authority}${it.host}"
            }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }

    private val htmlRequestActivityResult =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                methodWidget?.evaluateJavascript(
                    result.data?.getStringExtra(Constants.EXTRA_KEY_DATA).orEmpty()
                )
            }
        }

    private val messageEventHandler: (String, JSONObject) -> Unit = { eventName, params ->
        val paymentMethodKey = try {
            params.getString(
                PaymentWidgetContainer.EVENT_PARAM_PAYMENT_METHOD_KEY
            ).orEmpty()
        } catch (e: Exception) {
            ""
        }

        when (eventName) {
            PaymentMethod.EVENT_NAME_CUSTOM_REQUESTED -> {
                methodEventCallback?.onCustomRequested(paymentMethodKey)
            }
            PaymentMethod.EVENT_NAME_CUSTOM_METHOD_SELECTED -> {
                methodEventCallback?.onCustomPaymentMethodSelected(paymentMethodKey)
            }
            PaymentMethod.EVENT_NAME_CUSTOM_METHOD_UNSELECTED -> {
                methodEventCallback?.onCustomPaymentMethodUnselected(paymentMethodKey)
            }
            Agreement.EVENT_NAME_UPDATE_AGREEMENT_STATUS -> {
                kotlin.runCatching { AgreementStatus.fromJson(params) }.getOrNull()?.let {
                    agreementCallback?.onAgreementStatusChanged(it)
                }
            }
        }
    }

    private val methodWidgetJavascriptInterface
        get() = object : PaymentWidgetJavascriptInterface(methodWidget, messageEventHandler) {
            @JavascriptInterface
            fun requestPayments(html: String) {
                methodWidget?.context?.let {
                    handlePaymentDom(it, html)
                }
            }

            @JavascriptInterface
            fun requestHTML(html: String) {
                methodWidget?.context?.let {
                    htmlRequestActivityResult.launch(
                        TossPaymentsWebActivity.getIntent(it, domain, html)
                    )
                }
            }
        }

    private var methodWidget: PaymentMethod? = null
    private var agreementWidget: Agreement? = null

    private var orderId: String = ""
    private var requestCode: Int? = null
    private var paymentResultLauncher: ActivityResultLauncher<Intent>? = null

    private var methodEventCallback: PaymentMethodCallback? = null
    private var agreementCallback: AgreementCallback? = null

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

    @Deprecated("This function is no longer needed", level = DeprecationLevel.ERROR)
    fun setMethodWidget(methodWidget: PaymentMethod) {
    }

    /**
     * This function has been deprecated because it is no longer needed. Use [renderPaymentMethods] instead.
     */
    @Deprecated(
        "This function is no longer needed. Use renderPaymentMethods instead.",
        replaceWith = ReplaceWith("com.tosspayments.paymentsdk.PaymentWidget.renderPaymentMethods(methodWidget, amount)"),
        level = DeprecationLevel.ERROR
    )
    fun renderPaymentMethodWidget(amount: Number, orderId: String) {
    }

    fun renderPaymentMethods(
        method: PaymentMethod,
        amount: Number
    ) {
        this.methodWidget = method.apply {
            addJavascriptInterface(methodWidgetJavascriptInterface)
        }

        method.renderPaymentMethods(
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

    @JvmOverloads
    fun updateAmount(amount: Number, description: String = "") {
        methodWidget?.updateAmount(amount, description)
            ?: throw IllegalAccessException("Payment method widget is not set")
    }

    @Throws(IllegalAccessException::class)
    fun addMethodWidgetEventListener(callback: PaymentMethodCallback) {
        methodWidget?.run {
            methodEventCallback = callback
        } ?: kotlin.run {
            methodEventCallback = null
            throw IllegalAccessException("PaymentMethod is not rendered.")
        }
    }

    fun renderAgreement(agreement: Agreement) {
        this.agreementWidget = agreement

        agreement.apply {
            addJavascriptInterface(PaymentWidgetJavascriptInterface(agreement, messageEventHandler))
        }.renderAgreement(clientKey, customerKey, domain)
    }

    fun onAgreementStatusChanged(callback: AgreementCallback) {
        agreementWidget?.run {
            agreementCallback = callback
        } ?: kotlin.run {
            agreementCallback = null
            throw IllegalAccessException("Agreement is not rendered.")
        }
    }
}