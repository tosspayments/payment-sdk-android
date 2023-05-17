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
            Uri.parse(redirectUrl).host
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

    private val paymentResultLauncher: ActivityResultLauncher<Intent> =
        TossPayments.getPaymentResultLauncher(
            activity,
            {
                paymentCallback?.onPaymentSuccess(it)
            },
            {
                paymentCallback?.onPaymentFailed(it)
            })

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
                paymentMethodEventListener?.onCustomRequested(paymentMethodKey)
            }
            PaymentMethod.EVENT_NAME_CUSTOM_METHOD_SELECTED -> {
                paymentMethodEventListener?.onCustomPaymentMethodSelected(paymentMethodKey)
            }
            PaymentMethod.EVENT_NAME_CUSTOM_METHOD_UNSELECTED -> {
                paymentMethodEventListener?.onCustomPaymentMethodUnselected(paymentMethodKey)
            }
            Agreement.EVENT_NAME_UPDATE_AGREEMENT_STATUS -> {
                kotlin.runCatching { AgreementStatus.fromJson(params) }.getOrNull()?.let {
                    agreementStatusListener?.onAgreementStatusChanged(it)
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

    private var paymentMethodEventListener: PaymentMethodEventListener? = null
    private var agreementStatusListener: AgreementStatusListener? = null
    private var paymentCallback: PaymentCallback? = null

    private fun handlePaymentDom(context: Context, paymentHtml: String) {
        if (paymentHtml.isNotBlank()) {
            tossPayments.requestPayment(
                context = context,
                paymentHtml = paymentHtml,
                orderId = orderId,
                paymentResultLauncher = paymentResultLauncher,
                domain = domain
            )
        }
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
            domain,
            redirectUrl
        )
    }

    @JvmOverloads
    @Throws(IllegalAccessException::class)
    fun requestPayment(
        orderId: String,
        orderName: String,
        customerEmail: String? = null,
        customerName: String? = null,
        paymentCallback: PaymentCallback
    ) {
        try {
            this.paymentCallback = paymentCallback
            this.orderId = orderId

            methodWidget?.requestPayment(
                orderId,
                orderName,
                customerEmail,
                customerName,
                redirectUrl
            )
        } catch (e: Exception) {
            this.paymentCallback = null
            this.orderId = ""

            throw e
        }
    }

    @JvmOverloads
    @Throws(IllegalAccessException::class)
    fun updateAmount(amount: Number, description: String = "") {
        methodWidget?.updateAmount(amount, description)
    }

    @Throws(IllegalAccessException::class)
    fun addPaymentMethodEventListener(listener: PaymentMethodEventListener) {
        methodWidget?.run {
            paymentMethodEventListener = listener
        } ?: kotlin.run {
            paymentMethodEventListener = null
            throw IllegalAccessException(PaymentMethod.MESSAGE_NOT_RENDERED)
        }
    }

    fun renderAgreement(agreement: Agreement) {
        this.agreementWidget = agreement

        agreement.apply {
            addJavascriptInterface(PaymentWidgetJavascriptInterface(agreement, messageEventHandler))
        }.renderAgreement(clientKey, customerKey)
    }

    @Throws(IllegalAccessException::class)
    fun addAgreementStatusListener(listener: AgreementStatusListener) {
        agreementWidget?.run {
            agreementStatusListener = listener
        } ?: kotlin.run {
            agreementStatusListener = null
            throw IllegalAccessException(Agreement.MESSAGE_NOT_RENDERED)
        }
    }

    @Deprecated("This function is no longer needed", level = DeprecationLevel.ERROR)
    fun setMethodWidget(methodWidget: PaymentMethod) {
    }

    @Deprecated(
        "This function is no longer needed. Use renderPaymentMethods instead.",
        replaceWith = ReplaceWith("com.tosspayments.paymentsdk.PaymentWidget.renderPaymentMethods(methodWidget, amount)"),
        level = DeprecationLevel.ERROR
    )
    fun renderPaymentMethodWidget(amount: Number, orderId: String) {
    }

    @Deprecated(
        "This function is no longer needed. Use requestPayment instead.",
        replaceWith = ReplaceWith("com.tosspayments.paymentsdk.PaymentWidget.requestPayment()"),
        level = DeprecationLevel.ERROR
    )
    fun requestPayment(
        paymentResultLauncher: ActivityResultLauncher<Intent>,
        orderId: String,
        orderName: String,
        customerEmail: String? = null,
        customerName: String? = null
    ) {
    }

    @Deprecated(
        "This function is no longer needed. Use requestPayment instead.",
        replaceWith = ReplaceWith("com.tosspayments.paymentsdk.PaymentWidget.requestPayment()"),
        level = DeprecationLevel.ERROR
    )
    fun requestPayment(
        requestCode: Int,
        orderId: String,
        orderName: String,
        customerEmail: String? = null,
        customerName: String? = null
    ) {
    }
}