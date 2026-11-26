package com.tosspayments.paymentsdk.interfaces

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentWidgetMessageTest {
    private val received = mutableListOf<Pair<String, JSONObject>>()
    private val widgetInterface = PaymentWidgetJavascriptInterface(null) { name, params ->
        received.add(name to params)
    }

    @Test
    fun `custom payment method event reaches the handler with its params`() {
        widgetInterface.message("""{"name":"customRequest","params":{"paymentMethodKey":"point"}}""")

        assertEquals(1, received.size)
        assertEquals("customRequest", received[0].first)
        assertEquals("point", received[0].second.getString("paymentMethodKey"))
    }

    @Test
    fun `agreement status event reaches the handler with its params`() {
        widgetInterface.message(
            """{"name":"updateAgreementStatus","params":{"agreedRequiredTerms":true,"terms":[]}}"""
        )

        assertEquals("updateAgreementStatus", received.single().first)
        assertTrue(received.single().second.getBoolean("agreedRequiredTerms"))
    }

    @Test
    fun `height update is handled by the widget and not passed to the handler`() {
        widgetInterface.message("""{"name":"updateHeight","params":{"height":320.5}}""")

        assertTrue(received.isEmpty())
    }

    @Test
    fun `widget status is handled by the widget and not passed to the handler`() {
        widgetInterface.message(
            """{"name":"widgetStatus","params":{"widget":"paymentMethods","status":"load"}}"""
        )

        assertTrue(received.isEmpty())
    }

    @Test
    fun `message without params is ignored`() {
        widgetInterface.message("""{"name":"customRequest"}""")

        assertTrue(received.isEmpty())
    }

    @Test
    fun `message without a name is ignored`() {
        widgetInterface.message("""{"params":{"paymentMethodKey":"point"}}""")

        assertTrue(received.isEmpty())
    }

    @Test
    fun `message that is not json is ignored`() {
        widgetInterface.message("undefined")
        widgetInterface.message("")

        assertTrue(received.isEmpty())
    }
}
