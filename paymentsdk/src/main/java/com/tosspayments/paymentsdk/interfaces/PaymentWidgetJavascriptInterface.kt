package com.tosspayments.paymentsdk.interfaces

import android.webkit.JavascriptInterface
import com.tosspayments.paymentsdk.view.PaymentWidgetContainer
import org.json.JSONObject

open class PaymentWidgetJavascriptInterface(
    private val paymentWidget: PaymentWidgetContainer? = null,
    private val messageHandler: ((String, JSONObject) -> Unit)? = null
) {
    @JavascriptInterface
    fun message(json: String) {
        try {
            val jsonObject = JSONObject(json)
            val eventName = jsonObject.getString(PaymentWidgetContainer.EVENT_NAME)
            val params = jsonObject.getJSONObject(PaymentWidgetContainer.EVENT_PARAMS)

            when (eventName) {
                PaymentWidgetContainer.EVENT_NAME_UPDATE_HEIGHT -> {
                    paymentWidget?.updateHeight(
                        params.getDouble(PaymentWidgetContainer.EVENT_PARAM_HEIGHT).toFloat()
                    )
                }
                PaymentWidgetContainer.EVENT_NAME_WIDGET_STATUS -> {
                    paymentWidget?.updateWidgetStatus(
                        params.getString(PaymentWidgetContainer.EVENT_PARAM_WIDGET),
                        params.getString(PaymentWidgetContainer.EVENT_PARAM_STATUS)
                    )
                }
                else -> {
                    messageHandler?.invoke(eventName, params)
                }
            }
        } catch (ignore: Exception) {
        }
    }
}