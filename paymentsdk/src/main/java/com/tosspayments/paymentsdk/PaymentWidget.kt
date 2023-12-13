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
    private var selectedPaymentMethod: SelectedPaymentMethod? = null

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
            PaymentMethod.EVENT_NAME_CHANGE_PAYMENT_METHOD -> {
                kotlin.runCatching { SelectedPaymentMethod.fromJson(params) }.getOrNull()?.let {
                    selectedPaymentMethod = it
                }
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
                methodWidget?.let {
                    handlePaymentDom(it.context, it.orderId, html)
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

            @JavascriptInterface
            fun error(errorCode: String, message: String, orderId: String?) {
                methodWidget?.onFail(TossPaymentResult.Fail(errorCode, message, orderId))
            }
        }

    private var methodWidget: PaymentMethod? = null
    private var agreementWidget: Agreement? = null

    private var paymentMethodEventListener: PaymentMethodEventListener? = null
    private var agreementStatusListener: AgreementStatusListener? = null
    private var paymentCallback: PaymentCallback? = null

    private fun handlePaymentDom(context: Context, orderId: String, paymentHtml: String) {
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

    /**
     * 결제 수단 위젯 렌더링
     * @param method : 결제 수단 위젯
     * @param amount : 결제 금액
     * @param options : 결제위젯의 렌더링 옵션
     * @param paymentWidgetStatusListener : 결제위젯 렌더링 이벤트 리스너
     * @since 2023/05/19
     */
    @JvmOverloads
    fun renderPaymentMethods(
        method: PaymentMethod,
        amount: Number,
        options: PaymentMethod.Rendering.Options? = null,
        paymentWidgetStatusListener: PaymentWidgetStatusListener? = null
    ) {
        renderPaymentMethods(
            method,
            PaymentMethod.Rendering.Amount(value = amount),
            options,
            paymentWidgetStatusListener
        )
    }

    /**
     * PayPal 해외간편결제 연동 지원 결제 수단 위젯 렌더링.
     * @param method : 결제 수단 위젯
     * @param amount : 결제 금액 정보
     * @param options : 결제위젯 렌더링 옵션
     * @param paymentWidgetStatusListener : 결제위젯 렌더링 이벤트 리스너
     * @since 2023/07/04
     */
    @JvmOverloads
    fun renderPaymentMethods(
        method: PaymentMethod,
        amount: PaymentMethod.Rendering.Amount,
        options: PaymentMethod.Rendering.Options? = null,
        paymentWidgetStatusListener: PaymentWidgetStatusListener? = null
    ) {
        this.methodWidget = method.apply {
            addPaymentWidgetStatusListener(paymentWidgetStatusListener)
        }

        methodWidget?.addJavascriptInterface(methodWidgetJavascriptInterface)

        method.renderPaymentMethods(
            clientKey,
            customerKey,
            amount,
            options,
            domain,
            redirectUrl
        )
    }

    /**
     * 고객이 선택한 결제수단
     * @since 2023/10/06
     */
    @Throws(IllegalAccessException::class)
    fun getSelectedPaymentMethod(): SelectedPaymentMethod {
        return this.selectedPaymentMethod ?: throw IllegalAccessException(PaymentMethod.MESSAGE_NOT_RENDERED)
    }

    /**
     * 결제 요청
     * @param paymentInfo : 결제 정보
     * @param paymentCallback : 결제 결과 수신 Callback
     * @since 2023/05/19
     */
    @Throws(IllegalAccessException::class)
    fun requestPayment(
        paymentInfo: PaymentMethod.PaymentInfo,
        paymentCallback: PaymentCallback
    ) {
        try {
            this.paymentCallback = paymentCallback
            methodWidget?.requestPayment(paymentInfo)
        } catch (e: Exception) {
            this.paymentCallback = null
            throw e
        }
    }

    /**
     * 결제 금액 갱신
     * @param amount : 신규 결제 금액
     * @param description : 결제 금액이 변경된 사유. 결제 전환율 개선을 위한 리포트 및 A/B 테스트 분석에 사용. 쿠폰와 같은 형식.
     * @since 2023/05/19
     */
    @JvmOverloads
    @Throws(IllegalAccessException::class)
    fun updateAmount(amount: Number, description: String? = null) {
        methodWidget?.updateAmount(amount, description.orEmpty())
    }

    /**
     * 커스텀 결제수단 또는 간편결제 직연동 이벤트 리스너 추가
     * @param listener : customRequest, customPaymentMethodSelect, customPaymentMethodUnselect 이벤트 수신 리스너
     * @since 2023/05/19
     */
    @Throws(IllegalAccessException::class)
    fun addPaymentMethodEventListener(listener: PaymentMethodEventListener) {
        methodWidget?.run {
            paymentMethodEventListener = listener
        } ?: kotlin.run {
            paymentMethodEventListener = null
            throw IllegalAccessException(PaymentMethod.MESSAGE_NOT_RENDERED)
        }
    }

    /**
     * 이용약관 위젯 렌더링
     * @param agreement : 이용약관 위젯
     * @param paymentWidgetStatusListener : 이용약관 위젯 렌더링 이벤트 리스너
     * @since 2023/05/19
     */
    @JvmOverloads
    fun renderAgreement(
        agreement: Agreement,
        paymentWidgetStatusListener: PaymentWidgetStatusListener? = null
    ) {
        this.agreementWidget = agreement.apply {
            addPaymentWidgetStatusListener(paymentWidgetStatusListener)
        }

        agreement.apply {
            addJavascriptInterface(PaymentWidgetJavascriptInterface(agreement, messageEventHandler))
        }.renderAgreement(clientKey, customerKey)
    }

    /**
     * 이용약관 상태 변경 수신 리스너 추가
     * @param listener : 이용약관 상태 변경 수신 리스너
     * @since 2023/05/19
     */
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