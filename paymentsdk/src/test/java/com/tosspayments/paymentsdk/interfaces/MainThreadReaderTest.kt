package com.tosspayments.paymentsdk.interfaces

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainThreadReaderTest {
    private val mainThread = Executors.newSingleThreadExecutor()
    private val reader = MainThreadReader(TIMEOUT_MILLIS) { mainThread.execute(it) }

    @After
    fun tearDown() {
        mainThread.shutdownNow()
    }

    @Test
    fun `value is read on the main thread`() {
        val mainThreadName = mainThread.submit<String> { Thread.currentThread().name }.get()

        assertEquals(mainThreadName, reader.read { Thread.currentThread().name })
    }

    @Test
    fun `nothing is read when the main thread stays busy past the timeout`() {
        val release = CountDownLatch(1)
        mainThread.execute { release.await() }

        assertNull(reader.read { "https://appassets.androidplatform.net" })

        release.countDown()
    }

    @Test
    fun `main thread that waits for a bridge call is released by that call`() {
        val origin = BridgeOrigin("https", "appassets.androidplatform.net", BridgeOrigin.NO_PORT)
        val called = CountDownLatch(1)
        val bridge = PaymentBridge(
            TOKEN,
            origin,
            listOf(BridgeObject("TossPayment", mapOf("success" to { called.countDown() })))
        )
        val released = mainThread.submit<Boolean> { called.await(WAIT_MILLIS, TimeUnit.MILLISECONDS) }

        bridge.receive(TOKEN, "TossPayment", "success", """["done"]""") { reader.read { origin } }

        assertTrue(released.get())
    }

    private companion object {
        const val TIMEOUT_MILLIS = 200L
        const val WAIT_MILLIS = 5000L
        const val TOKEN = "3f9a1c0b7d2e4f60819a2b3c4d5e6f708192a3b4c5d6e7f8091a2b3c4d5e6f70"
    }
}
