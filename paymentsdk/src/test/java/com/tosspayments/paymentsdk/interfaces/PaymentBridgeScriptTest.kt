package com.tosspayments.paymentsdk.interfaces

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mozilla.javascript.Context
import org.mozilla.javascript.ScriptableObject

class PaymentBridgeScriptTest {
    private val calls = mutableListOf<Pair<String, List<String?>>>()
    private val loadedOrigin = BridgeOrigin("https", "appassets.androidplatform.net", BridgeOrigin.NO_PORT)
    private val objects = listOf(
        BridgeObject(
            "PaymentWidgetAndroidSDK",
            mapOf(
                "message" to { arguments -> record("widget.message", arguments) },
                "error" to { arguments -> record("widget.error", arguments) }
            )
        ),
        BridgeObject(
            "TossPayment",
            mapOf("success" to { arguments -> record("payment.success", arguments) })
        )
    )
    private val bridge = PaymentBridge.open(loadedOrigin, objects)
    private val page = Page(bridge.addScript("<!DOCTYPE html><html></html>")) { token, target, method, arguments ->
        bridge.receive(token, target, method, arguments) { loadedOrigin }
    }

    @After
    fun tearDown() {
        Context.exit()
    }

    @Test
    fun `document gets each object with its own methods`() {
        assertEquals("message,error", page.run("Object.keys(window.PaymentWidgetAndroidSDK).join()"))
        assertEquals("success", page.run("Object.keys(window.TossPayment).join()"))
    }

    @Test
    fun `call on an object reaches its method with the arguments in order`() {
        page.run("window.PaymentWidgetAndroidSDK.error('INVALID_ORDER_ID', 'invalid', 'order-1')")

        assertEquals(
            listOf("widget.error" to listOf<String?>("INVALID_ORDER_ID", "invalid", "order-1")),
            calls
        )
    }

    @Test
    fun `arguments arrive as the previous interface passed them`() {
        page.run("window.PaymentWidgetAndroidSDK.error(undefined, null, 1500)")
        page.run("window.TossPayment.success(true)")

        assertEquals(
            listOf(
                "widget.error" to listOf("undefined", null, "1500"),
                "payment.success" to listOf<String?>("true")
            ),
            calls
        )
    }

    @Test
    fun `script takes its own tag out of the document`() {
        assertEquals(1.0, page.run("removedScripts.length"))
    }

    @Test
    fun `value of the load is not readable from the page`() {
        val token = page.tokens().single()

        assertEquals("undefined", page.run("typeof token"))
        assertFalse(page.run("String(window.TossPayment.success)").toString().contains(token))
        assertFalse(page.run("JSON.stringify(window.TossPayment)").toString().contains(token))
    }

    @Test
    fun `each load gets its own value`() {
        val reloaded = PaymentBridge.open(loadedOrigin, objects)
        val reloadedPage = Page(reloaded.addScript("<html></html>")) { _, _, _, _ -> }

        assertTrue(Regex("[0-9a-f]{64}").matches(page.tokens().single()))
        assertNotEquals(page.tokens().single(), reloadedPage.tokens().single())
    }

    @Test
    fun `call from the document of an earlier load is dropped`() {
        val reloaded = PaymentBridge.open(loadedOrigin, objects)
        val earlierPage = Page(bridge.addScript("<html></html>")) { token, target, method, arguments ->
            reloaded.receive(token, target, method, arguments) { loadedOrigin }
        }

        earlierPage.run("window.TossPayment.success('done')")

        assertTrue(calls.isEmpty())
    }

    private fun record(name: String, arguments: List<String?>) {
        calls.add(name to arguments)
    }

    class Entry(private val receive: (String?, String?, String?, String?) -> Unit) {
        val tokens = mutableListOf<String>()

        fun invoke(token: String?, target: String?, method: String?, arguments: String?) {
            tokens.add(token.orEmpty())
            receive(token, target, method, arguments)
        }
    }

    private class Page(html: String, receive: (String?, String?, String?, String?) -> Unit) {
        private val context = Context.enter().apply { optimizationLevel = -1 }
        private val scope = context.initStandardObjects()
        private val entry = Entry(receive)

        init {
            ScriptableObject.putProperty(scope, "window", scope)
            ScriptableObject.putProperty(scope, PaymentBridge.ENTRY_NAME, Context.javaToJS(entry, scope))
            run(
                """
                var removedScripts = [];
                var document = {
                  currentScript: {
                    parentNode: { removeChild: function (node) { removedScripts.push(node); } }
                  }
                };
                """.trimIndent()
            )
            run(html.substringAfter("<script>").substringBefore("</script>"))
        }

        fun run(script: String): Any? {
            return context.evaluateString(scope, script, "page", 1, null)
        }

        fun tokens(): List<String> {
            run("window.TossPayment.success('probe')")

            return entry.tokens.distinct()
        }
    }
}
