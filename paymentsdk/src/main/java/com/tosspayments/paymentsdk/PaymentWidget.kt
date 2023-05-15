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
import com.tosspayments.paymentsdk.view.PaymentWidgetContainer
import org.json.JSONObject

class PaymentWidget(
    activity: AppCompatActivity,
    private val clientKey: String,
    private val customerKey: String,
    options: PaymentWidgetOptions? = null
) {
    private val eventHandlerMap = mutableMapOf<String, (String) -> Unit>()
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

    private val methodWidgetJavascriptInterface = object : PaymentWidgetJavascriptInterface() {
        @JavascriptInterface
        override fun message(json: String) {
            handleJavascriptMessage(methodWidget, json)
        }

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

    @Deprecated("This function is no longer needed", level = DeprecationLevel.ERROR)
    fun setMethodWidget(methodWidget: PaymentMethodWidget) {
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

    fun renderPaymentMethods(methodWidget: PaymentMethodWidget, amount: Number) {
        this.methodWidget = methodWidget.apply {
            addJavascriptInterface(methodWidgetJavascriptInterface)
        }

        methodWidget.renderPaymentMethods(
            clientKey,
            customerKey,
            amount,
            redirectUrl
        )
    }

    private fun handleJavascriptMessage(widgetContainer: PaymentWidgetContainer?, json: String) {
        try {
            val jsonObject = JSONObject(json)
            val eventName = jsonObject.getString(PaymentWidgetContainer.EVENT_NAME)
            val params = jsonObject.getJSONObject(PaymentWidgetContainer.EVENT_PARAMS)

            when (eventName) {
                PaymentWidgetContainer.EVENT_NAME_UPDATE_HEIGHT -> {
                    updateHeight(
                        widgetContainer,
                        params.getDouble(PaymentWidgetContainer.EVENT_PARAM_HEIGHT).toFloat()
                    )
                }
                else -> {
                    eventHandlerMap[eventName]?.invoke(params.getString(PaymentWidgetContainer.EVENT_PARAM_PAYMENT_METHOD_KEY))
                }
            }
        } catch (ignore: Exception) {
        }
    }

    private fun updateHeight(widgetContainer: PaymentWidgetContainer?, height: Float?) {
        widgetContainer?.updateHeight(height)
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

    fun onCustomRequested(paymentMethodKeyHandler: (String) -> Unit) {
        addMethodWidgetEventListener("customRequest", paymentMethodKeyHandler)
    }

    fun onCustomPaymentMethodSelect(paymentMethodKeyHandler: (String) -> Unit) {
        addMethodWidgetEventListener("customPaymentMethodSelect", paymentMethodKeyHandler)
    }

    fun onCustomPaymentMethodUnselected(paymentMethodKeyHandler: (String) -> Unit) {
        addMethodWidgetEventListener("customPaymentMethodUnselect", paymentMethodKeyHandler)
    }

    fun addMethodWidgetEventListener(eventName: String, paymentMethodKeyHandler: (String) -> Unit) {
        methodWidget?.let {
            eventHandlerMap[eventName] = paymentMethodKeyHandler
        } ?: kotlin.run {
            throw IllegalAccessException("Payment method widget is not rendered.")
        }
    }

    fun renderAgreement() {

    }
}