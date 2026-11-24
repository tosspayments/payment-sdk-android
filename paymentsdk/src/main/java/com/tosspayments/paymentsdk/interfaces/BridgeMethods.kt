package com.tosspayments.paymentsdk.interfaces

import com.tosspayments.paymentsdk.model.TossPaymentResult

internal fun textBridgeMethod(
    name: String,
    receive: (String) -> Unit
): Pair<String, (List<String?>) -> Unit> {
    return name to { arguments -> receive(arguments[0]!!) }
}

internal fun errorBridgeMethod(
    receive: (TossPaymentResult.Fail) -> Unit
): Pair<String, (List<String?>) -> Unit> {
    return "error" to { arguments ->
        receive(
            TossPaymentResult.Fail(
                errorCode = arguments[0]!!,
                errorMessage = arguments[1]!!,
                orderId = arguments[2]
            )
        )
    }
}
