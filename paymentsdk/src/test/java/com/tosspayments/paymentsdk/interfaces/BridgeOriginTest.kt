package com.tosspayments.paymentsdk.interfaces

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URI

class BridgeOriginTest {
    private val tosspayments = base("https://tosspayments.com")
    private val brandpay = base("https://Shop.Example.com/brandpay/callback?x=1")
    private val brandpayWithPort = base("https://shop.example.com:8443/callback")
    private val brandpayWithDefaultPort = base("https://shop.example.com:443/callback")
    private val brandpayOverHttp = base("http://shop.example.com/callback")

    @Test
    fun `main frame on the default base URL`() {
        expect(tosspayments, sender("https", "tosspayments.com"), true)
    }

    @Test
    fun `main frame on the brandpay redirect host`() {
        expect(brandpay, sender("https", "shop.example.com"), true)
    }

    @Test
    fun `scheme and host compare without case`() {
        expect(brandpay, sender("HTTPS", "SHOP.EXAMPLE.COM"), true)
    }

    @Test
    fun `same explicit port`() {
        expect(brandpayWithPort, sender("https", "shop.example.com", 8443), true)
    }

    @Test
    fun `default port when the base URL has another port`() {
        expect(brandpayWithPort, sender("https", "shop.example.com"), false)
    }

    @Test
    fun `explicit default port`() {
        expect(tosspayments, sender("https", "tosspayments.com", 443), true)
    }

    @Test
    fun `port 80 is not the default port of https`() {
        expect(tosspayments, sender("https", "tosspayments.com", 80), false)
    }

    @Test
    fun `explicit default port on the base URL`() {
        expect(brandpayWithDefaultPort, sender("https", "shop.example.com"), true)
    }

    @Test
    fun `http base URL accepts its own origin`() {
        expect(brandpayOverHttp, sender("http", "shop.example.com"), true)
    }

    @Test
    fun `explicit default port on http`() {
        expect(brandpayOverHttp, sender("http", "shop.example.com", 80), true)
    }

    @Test
    fun `http base URL rejects https`() {
        expect(brandpayOverHttp, sender("https", "shop.example.com"), false)
    }

    @Test
    fun `main frame after navigating to a card issuer`() {
        expect(tosspayments, sender("https", "card.example.com"), false)
    }

    @Test
    fun `scheme mismatch`() {
        expect(tosspayments, sender("http", "tosspayments.com"), false)
    }

    @Test
    fun `host with the base host as prefix`() {
        expect(tosspayments, sender("https", "tosspayments.com.example.com"), false)
    }

    @Test
    fun `host with the base host as suffix`() {
        expect(tosspayments, sender("https", "nottosspayments.com"), false)
    }

    @Test
    fun `subdomain of the base host`() {
        expect(tosspayments, sender("https", "sub.tosspayments.com"), false)
    }

    @Test
    fun `about blank before the HTML loads`() {
        expect(tosspayments, sender("about", ""), false)
    }

    @Test
    fun `opaque origin`() {
        expect(tosspayments, sender("", ""), false)
    }

    @Test
    fun `base URL without a host accepts nothing`() {
        expect(base("about:blank"), sender("about", ""), false)
    }

    @Test
    fun `file base URL accepts nothing`() {
        expect(base("file:///var/containers/index.html"), sender("file", ""), false)
    }

    @Test
    fun `data base URL accepts nothing`() {
        expect(base("data:text/html,hello"), sender("data", ""), false)
    }

    @Test
    fun `base URL that is neither https nor http accepts nothing`() {
        expect(base("myapp://callback"), sender("myapp", "callback"), false)
    }

    @Test
    fun `base URL without a scheme accepts nothing`() {
        expect(base("callback"), sender("", ""), false)
    }

    private fun expect(allowed: BridgeOrigin, origin: BridgeOrigin, expected: Boolean) {
        assertEquals(expected, allowed.allowsMessageFrom(origin))
    }

    private fun base(url: String): BridgeOrigin {
        val uri = URI(url)

        return BridgeOrigin(uri.scheme, uri.host, uri.port)
    }

    private fun sender(scheme: String, host: String, port: Int = BridgeOrigin.NO_PORT): BridgeOrigin {
        return BridgeOrigin(scheme, host, port)
    }
}
