package com.tosspayments.paymentsdk.model.paymentinfo

import com.tosspayments.paymentsdk.view.PaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SubOrderPayloadTest {
    @Test
    fun `payment window payload includes sub orders`() {
        val paymentInfo = TossCardPaymentInfo(
            orderId = "order-id",
            orderName = "main order",
            amount = 15000L
        ).apply {
            subOrders = listOf(sampleSubOrder(detailAddress = "101호"))
        }

        val payload = paymentInfo.getPayload()
        val subOrders = payload.getJSONArray("subOrders")
        val subOrder = subOrders.getJSONObject(0)
        val address = subOrder.getJSONObject("merchantAddress")

        assertEquals(1, subOrders.length())
        assertEquals("1234567890", subOrder.getString("merchantBusinessNumber"))
        assertEquals("테스트상점", subOrder.getString("merchantName"))
        assertEquals("하위 주문명", subOrder.getString("orderName"))
        assertEquals("KR", address.getString("country"))
        assertEquals("06236", address.getString("postalCode"))
        assertEquals("서울특별시 강남구 테헤란로 123", address.getString("address"))
        assertEquals("101호", address.getString("detailAddress"))
    }

    @Test
    fun `payment widget payload includes sub orders without amount`() {
        val paymentInfo = PaymentMethod.PaymentInfo(
            orderId = "widget-order-id",
            orderName = "widget main order"
        ).apply {
            subOrders = listOf(sampleSubOrder(detailAddress = null))
        }

        val payload = paymentInfo.getPayload()
        val subOrder = payload.getJSONArray("subOrders").getJSONObject(0)
        val address = subOrder.getJSONObject("merchantAddress")

        assertFalse(payload.has("amount"))
        assertEquals("1234567890", subOrder.getString("merchantBusinessNumber"))
        assertEquals("테스트상점", subOrder.getString("merchantName"))
        assertEquals("하위 주문명", subOrder.getString("orderName"))
        assertEquals("KR", address.getString("country"))
        assertEquals("06236", address.getString("postalCode"))
        assertEquals("서울특별시 강남구 테헤란로 123", address.getString("address"))
        assertFalse(address.has("detailAddress"))
    }

    @Test
    fun `payload omits sub orders when not set`() {
        val paymentInfo = TossPaymentInfo(
            orderId = "order-id",
            orderName = "main order",
            amount = 15000L
        )

        assertFalse(paymentInfo.getPayload().has("subOrders"))
    }

    private fun sampleSubOrder(detailAddress: String?): SubOrder {
        return SubOrder(
            merchantBusinessNumber = "1234567890",
            merchantName = "테스트상점",
            merchantAddress = MerchantAddress(
                country = "KR",
                postalCode = "06236",
                address = "서울특별시 강남구 테헤란로 123",
                detailAddress = detailAddress
            ),
            orderName = "하위 주문명"
        )
    }
}
