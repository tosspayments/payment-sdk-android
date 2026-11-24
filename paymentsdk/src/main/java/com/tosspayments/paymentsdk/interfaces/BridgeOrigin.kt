package com.tosspayments.paymentsdk.interfaces

internal class BridgeOrigin(scheme: String?, host: String?, port: Int) {
    private val scheme = scheme.orEmpty().lowercase()
    private val host = host.orEmpty().lowercase()
    private val port = normalizedPort(port, this.scheme)

    private val isOpaque: Boolean
        get() = host.isEmpty() || (scheme != "https" && scheme != "http")

    fun allowsMessage(fromMainFrame: Boolean, origin: BridgeOrigin): Boolean {
        return fromMainFrame && !isOpaque &&
                scheme == origin.scheme && host == origin.host && port == origin.port
    }

    companion object {
        const val NO_PORT = -1

        private fun normalizedPort(port: Int, scheme: String): Int {
            return when {
                port <= 0 -> NO_PORT
                scheme == "https" && port == 443 -> NO_PORT
                scheme == "http" && port == 80 -> NO_PORT
                else -> port
            }
        }
    }
}
