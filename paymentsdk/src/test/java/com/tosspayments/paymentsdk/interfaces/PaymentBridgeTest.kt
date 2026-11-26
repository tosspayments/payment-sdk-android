package com.tosspayments.paymentsdk.interfaces

import org.json.JSONException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentBridgeTest {
    private val calls = mutableListOf<Pair<String, List<String?>>>()
    private var mainFrameOriginReads = 0
    private val loadedOrigin = origin("appassets.androidplatform.net")
    private val bridge = PaymentBridge(
        TOKEN,
        loadedOrigin,
        listOf(
            BridgeObject(
                "PaymentWidgetAndroidSDK",
                mapOf(
                    "message" to { arguments -> record("widget.message", arguments) },
                    "error" to { arguments -> record("widget.error", arguments) },
                    "requestPayments" to { throw IllegalStateException("launcher is gone") }
                )
            ),
            BridgeObject(
                "TossPayment",
                mapOf("success" to { arguments -> record("payment.success", arguments) })
            )
        )
    )

    @Test
    fun `call with the token of the loaded document reaches its method`() {
        receive(TOKEN, loadedOrigin, "PaymentWidgetAndroidSDK", "message", """["{}"]""")

        assertEquals(listOf("widget.message" to listOf<String?>("{}")), calls)
    }

    @Test
    fun `each object receives only its own methods`() {
        receive(TOKEN, loadedOrigin, "TossPayment", "success", """["done"]""")
        assertThrows(NoSuchElementException::class.java) {
            receive(TOKEN, loadedOrigin, "TossPayment", "message", """["{}"]""")
        }
        assertThrows(NoSuchElementException::class.java) {
            receive(TOKEN, loadedOrigin, "PaymentWidgetAndroidSDK", "success", """["done"]""")
        }

        assertEquals(listOf("payment.success" to listOf<String?>("done")), calls)
    }

    @Test
    fun `null and missing arguments arrive as they were sent`() {
        receive(
            TOKEN,
            loadedOrigin,
            "PaymentWidgetAndroidSDK",
            "error",
            """["INVALID_ORDER_ID","invalid",null]"""
        )
        receive(TOKEN, loadedOrigin, "PaymentWidgetAndroidSDK", "error", """[]""")

        assertEquals(
            listOf(
                "widget.error" to listOf("INVALID_ORDER_ID", "invalid", null),
                "widget.error" to emptyList()
            ),
            calls
        )
    }

    @Test
    fun `iframe on another origin`() {
        receive(null, loadedOrigin, "PaymentWidgetAndroidSDK", "message", """["{}"]""")
        receive("guess", loadedOrigin, "PaymentWidgetAndroidSDK", "message", """["{}"]""")

        assertTrue(calls.isEmpty())
    }

    @Test
    fun `iframe on the same origin is told apart only by the token`() {
        receive(null, loadedOrigin, "PaymentWidgetAndroidSDK", "message", """["without"]""")
        receive(TOKEN, loadedOrigin, "PaymentWidgetAndroidSDK", "message", """["through the parent"]""")

        assertEquals(listOf("widget.message" to listOf<String?>("through the parent")), calls)
    }

    @Test
    fun `call without the token is dropped`() {
        receive(null, loadedOrigin, "PaymentWidgetAndroidSDK", "message", """["{}"]""")
        receive("", loadedOrigin, "PaymentWidgetAndroidSDK", "message", """["{}"]""")
        receive("undefined", loadedOrigin, "PaymentWidgetAndroidSDK", "message", """["{}"]""")

        assertTrue(calls.isEmpty())
    }

    @Test
    fun `call with another token is dropped`() {
        receive(TOKEN.reversed(), loadedOrigin, "PaymentWidgetAndroidSDK", "message", """["{}"]""")
        receive(TOKEN.dropLast(1), loadedOrigin, "PaymentWidgetAndroidSDK", "message", """["{}"]""")
        receive(TOKEN + "0", loadedOrigin, "PaymentWidgetAndroidSDK", "message", """["{}"]""")
        receive(TOKEN.uppercase(), loadedOrigin, "PaymentWidgetAndroidSDK", "message", """["{}"]""")

        assertTrue(calls.isEmpty())
    }

    @Test
    fun `call with the token of an earlier load is dropped`() {
        val reloaded = PaymentBridge(
            "f".repeat(64),
            loadedOrigin,
            listOf(
                BridgeObject(
                    "PaymentWidgetAndroidSDK",
                    mapOf("message" to { arguments -> record("widget.message", arguments) })
                )
            )
        )

        reloaded.receive(TOKEN, "PaymentWidgetAndroidSDK", "message", """["{}"]""") { loadedOrigin }

        assertTrue(calls.isEmpty())
    }

    @Test
    fun `call without the token does not read the main frame address`() {
        receive("guess", loadedOrigin, "PaymentWidgetAndroidSDK", "message", """["{}"]""")

        assertEquals(0, mainFrameOriginReads)
    }

    @Test
    fun `call is dropped when the main frame has moved to another origin`() {
        receive(TOKEN, origin("card.example.com"), "PaymentWidgetAndroidSDK", "message", """["{}"]""")

        assertTrue(calls.isEmpty())
    }

    @Test
    fun `call is dropped when the main frame has no address`() {
        receive(TOKEN, BridgeOrigin(null, null, BridgeOrigin.NO_PORT), "TossPayment", "success", """["done"]""")

        assertTrue(calls.isEmpty())
    }

    @Test
    fun `call with the token is handled when the main thread does not answer in time`() {
        receive(TOKEN, null, "TossPayment", "success", """["done"]""")

        assertEquals(listOf("payment.success" to listOf<String?>("done")), calls)
    }

    @Test
    fun `call without the token is dropped when the main thread does not answer in time`() {
        receive("guess", null, "TossPayment", "success", """["done"]""")

        assertTrue(calls.isEmpty())
    }

    @Test
    fun `call with the token to an unknown object or method fails`() {
        assertThrows(NoSuchElementException::class.java) {
            receive(TOKEN, loadedOrigin, "Unknown", "message", """["{}"]""")
        }
        assertThrows(NoSuchElementException::class.java) {
            receive(TOKEN, loadedOrigin, "PaymentWidgetAndroidSDK", "unknown", """["{}"]""")
        }
        assertThrows(NoSuchElementException::class.java) {
            receive(TOKEN, loadedOrigin, null, null, """["{}"]""")
        }
        assertTrue(calls.isEmpty())
    }

    @Test
    fun `call with the token whose arguments are not a list fails`() {
        listOf(null, "", "undefined", """{"0":"{}"}""").forEach { arguments ->
            assertThrows(JSONException::class.java) {
                receive(TOKEN, loadedOrigin, "PaymentWidgetAndroidSDK", "message", arguments)
            }
        }
        assertTrue(calls.isEmpty())
    }

    @Test
    fun `call without the token is dropped quietly whatever it carries`() {
        receive("guess", loadedOrigin, "Unknown", "unknown", "undefined")

        assertTrue(calls.isEmpty())
    }

    @Test
    fun `method failure reaches the caller as before`() {
        assertThrows(IllegalStateException::class.java) {
            receive(TOKEN, loadedOrigin, "PaymentWidgetAndroidSDK", "requestPayments", """["<html>"]""")
        }
    }

    @Test
    fun `main frame request for the document gets the script`() {
        val html = bridge.scriptedDocument(DOCUMENT_URL, DOCUMENT_URL, true) { "<html></html>" }

        assertTrue(html!!.startsWith("<script>"))
        assertTrue(html.endsWith("</script><html></html>"))
    }

    @Test
    fun `request for the document from a frame inside it gets no script`() {
        assertNull(bridge.scriptedDocument(DOCUMENT_URL, DOCUMENT_URL, false) { "<html></html>" })
    }

    @Test
    fun `request for another address gets no script`() {
        val other = "https://appassets.androidplatform.net/assets/other.html"

        assertNull(bridge.scriptedDocument(DOCUMENT_URL, other, true) { "<html></html>" })
        assertNull(bridge.scriptedDocument(DOCUMENT_URL, "$DOCUMENT_URL?x=1", true) { "<html></html>" })
    }

    @Test
    fun `html is left as it is when there is nothing to define`() {
        val empty = PaymentBridge(TOKEN, loadedOrigin, emptyList())

        assertEquals("<html></html>", empty.addScript("<html></html>"))
    }

    @Test
    fun `script goes right after the doctype`() {
        val html = bridge.addScript("<!DOCTYPE html><html><head></head></html>")

        assertTrue(html.startsWith("<!DOCTYPE html><script>"))
        assertTrue(html.endsWith("</script><html><head></head></html>"))
    }

    @Test
    fun `whitespace before the doctype stays before it`() {
        val html = bridge.addScript("\n    <!DOCTYPE html>\n    <html></html>")

        assertTrue(html.startsWith("\n    <!DOCTYPE html><script>"))
        assertTrue(html.endsWith("</script>\n    <html></html>"))
    }

    @Test
    fun `byte order mark before the doctype stays before it`() {
        val html = bridge.addScript("\uFEFF<!DOCTYPE html><html></html>")

        assertTrue(html.startsWith("\uFEFF<!DOCTYPE html><script>"))
    }

    @Test
    fun `byte order mark stays first when there is no doctype`() {
        val html = bridge.addScript("\uFEFF<html></html>")

        assertTrue(html.startsWith("\uFEFF<script>"))
        assertTrue(html.endsWith("</script><html></html>"))
    }

    @Test
    fun `doctype is found without case`() {
        val html = bridge.addScript("<!doctype html><html></html>")

        assertTrue(html.startsWith("<!doctype html><script>"))
    }

    @Test
    fun `script goes first when there is no doctype`() {
        val html = bridge.addScript(" <html> <head> </head> </html>")

        assertTrue(html.startsWith("<script>"))
        assertTrue(html.endsWith("</script> <html> <head> </head> </html>"))
    }

    @Test
    fun `doctype that is not at the start is left alone`() {
        val html = bridge.addScript("<html><!DOCTYPE html></html>")

        assertTrue(html.startsWith("<script>"))
        assertTrue(html.endsWith("</script><html><!DOCTYPE html></html>"))
    }

    @Test
    fun `script does not close its own tag early`() {
        val html = bridge.addScript("<html></html>")
        val script = html.substringAfter("<script>").substringBefore("<html>")

        assertEquals(1, Regex("</script>").findAll(script).count())
        assertFalse(script.contains("<!--"))
    }

    private fun receive(
        token: String?,
        mainFrameOrigin: BridgeOrigin?,
        target: String?,
        method: String?,
        arguments: String?
    ) {
        bridge.receive(token, target, method, arguments) {
            mainFrameOriginReads += 1
            mainFrameOrigin
        }
    }

    private fun record(name: String, arguments: List<String?>) {
        calls.add(name to arguments)
    }

    private fun origin(host: String): BridgeOrigin {
        return BridgeOrigin("https", host, BridgeOrigin.NO_PORT)
    }

    private companion object {
        const val TOKEN = "3f9a1c0b7d2e4f60819a2b3c4d5e6f708192a3b4c5d6e7f8091a2b3c4d5e6f70"
        const val DOCUMENT_URL = "https://appassets.androidplatform.net/assets/tosspayment_widget.html"
    }
}
