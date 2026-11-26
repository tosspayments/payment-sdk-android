package com.tosspayments.paymentsdk.interfaces

import com.tosspayments.paymentsdk.model.TossPaymentResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BridgeMethodsTest {
    private val failures = mutableListOf<TossPaymentResult.Fail>()
    private val texts = mutableListOf<String>()

    @Test
    fun `error carries the code, the message and the order id in that order`() {
        val (name, error) = errorBridgeMethod { failures.add(it) }

        error(listOf("INVALID_ORDER_ID", "주문번호가 올바르지 않습니다.", "order-1"))

        assertEquals("error", name)
        assertEquals("INVALID_ORDER_ID", failures.single().errorCode)
        assertEquals("주문번호가 올바르지 않습니다.", failures.single().errorMessage)
        assertEquals("order-1", failures.single().orderId)
    }

    @Test
    fun `error keeps an order id that was not given as null`() {
        val (_, error) = errorBridgeMethod { failures.add(it) }

        error(listOf("USER_CANCEL", "취소되었습니다.", null))

        assertNull(failures.single().orderId)
    }

    @Test
    fun `error without a code or a message fails`() {
        val (_, error) = errorBridgeMethod { failures.add(it) }

        assertThrows(NullPointerException::class.java) { error(listOf(null, "message", "order-1")) }
        assertThrows(NullPointerException::class.java) { error(listOf("CODE", null, "order-1")) }
        assertThrows(IndexOutOfBoundsException::class.java) { error(listOf("CODE", "message")) }
        assertTrue(failures.isEmpty())
    }

    @Test
    fun `text method carries its name and the first argument`() {
        val (name, success) = textBridgeMethod("success") { texts.add(it) }

        success(listOf("var params = '{}';"))

        assertEquals("success", name)
        assertEquals(listOf("var params = '{}';"), texts)
    }

    @Test
    fun `text method without its argument fails`() {
        val (_, requestPayments) = textBridgeMethod("requestPayments") { texts.add(it) }

        assertThrows(NullPointerException::class.java) { requestPayments(listOf(null)) }
        assertThrows(IndexOutOfBoundsException::class.java) { requestPayments(emptyList()) }
        assertTrue(texts.isEmpty())
    }
}
