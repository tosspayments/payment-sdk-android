package com.tosspayments.paymentsdk.model.paymentinfo

import org.json.JSONArray
import org.json.JSONObject

data class TossCardPaymentInfo(
    val orderId: String,
    val orderName: String,
    val amount: Long
) : TossPaymentInfo(orderId, orderName, amount) {
    var cardCompany: TossCardPaymentCompany? = null
    var cardInstallmentPlan: Int? = null
    var maxCardInstallmentPlan: Int? = null
    var freeInstallmentPlans: Map<TossCardPaymentCompany, List<Int>>? = null
    var useCardPoint: Boolean? = null
    var useAppCardOnly: Boolean? = null
    var useInternationalCardOnly: Boolean? = null
    var flowMode: TossCardPaymentFlow = TossCardPaymentFlow.DEFAULT
    var easyPay: TossEasyPayCompany? = null
    var discountCode: String? = null

    override val paymentPayload: JSONObject.(JSONObject) -> JSONObject
        get() = {
            cardCompany?.let { put("cardCompany", it.code) }
            cardInstallmentPlan?.let { put("cardInstallmentPlan", it) }
            maxCardInstallmentPlan?.let { put("maxCardInstallmentPlan", it) }
            useCardPoint?.let { put("useCardPoint", it) }
            useInternationalCardOnly?.let { put("useInternationalCardOnly", it) }

            freeInstallmentPlans?.let {
                val plans = JSONArray()

                it.keys.forEach { key ->
                    val company = key.name
                    val months = JSONArray()

                    it[key]?.forEach { month ->
                        months.put(month)
                    }

                    plans.put(JSONObject().apply {
                        put("company", company)
                        put("months", months)
                    })
                }

                put("freeInstallmentPlans", plans)
            }

            if (cardCompany?.appCardAvailable == true) {
                useAppCardOnly?.let { put("useAppCardOnly", it) }
            }

            if (flowMode == TossCardPaymentFlow.DIRECT) {
                easyPay?.let { put("easyPay", it) }
                discountCode?.let { put("discountCode", it) }
            }

            put("flowMode", flowMode)
        }
}