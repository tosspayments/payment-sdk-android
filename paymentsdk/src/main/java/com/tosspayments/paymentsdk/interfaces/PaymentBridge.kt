package com.tosspayments.paymentsdk.interfaces

import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom

internal class BridgeObject(
    val name: String,
    val methods: Map<String, (List<String?>) -> Unit>
)

internal class PaymentBridge(
    private val token: String,
    private val allowedOrigin: BridgeOrigin,
    private val objects: List<BridgeObject>
) {
    fun scriptedDocument(
        documentUrl: String,
        requestUrl: String,
        forMainFrame: Boolean,
        html: () -> String
    ): String? {
        return if (forMainFrame && requestUrl == documentUrl) addScript(html()) else null
    }

    fun addScript(html: String): String {
        if (objects.isEmpty()) {
            return html
        }

        val insertAt = LEADING_DOCTYPE.find(html)?.value?.length ?: 0

        return html.substring(0, insertAt) + "<script>$script</script>" + html.substring(insertAt)
    }

    fun receive(
        token: String?,
        target: String?,
        method: String?,
        arguments: String?,
        mainFrameOrigin: () -> BridgeOrigin?
    ) {
        if (!holds(token)) {
            return
        }

        val origin = mainFrameOrigin()

        if (origin != null && !allowedOrigin.allowsMessageFrom(origin)) {
            return
        }

        val handler = objects.first { it.name == target }.methods.getValue(method.orEmpty())
        val values = JSONArray(arguments.orEmpty())

        handler((0 until values.length()).map {
            if (values.isNull(it)) null else values.optString(it)
        })
    }

    private fun holds(token: String?): Boolean {
        return MessageDigest.isEqual(this.token.toByteArray(), token.orEmpty().toByteArray())
    }

    private val script: String
        get() {
            val exposures = objects.joinToString("") {
                "expose(${JSONObject.quote(it.name)}, ${JSONArray(it.methods.keys)});"
            }

            return """
                (function () {
                  var entry = window.$ENTRY_NAME;
                  var token = ${JSONObject.quote(token)};
                  function expose(name, methods) {
                    var target = {};
                    methods.forEach(function (method) {
                      target[method] = function () {
                        var values = Array.prototype.map.call(arguments, function (value) {
                          return value === null ? null : String(value);
                        });
                        entry.invoke(token, name, method, JSON.stringify(values));
                      };
                    });
                    window[name] = target;
                  }
                  $exposures
                  var script = document.currentScript;
                  if (script && script.parentNode) {
                    script.parentNode.removeChild(script);
                  }
                })();
            """.trimIndent()
        }

    companion object {
        const val ENTRY_NAME = "__tossPaymentsBridge"

        fun open(allowedOrigin: BridgeOrigin, objects: List<BridgeObject>): PaymentBridge {
            val token = ByteArray(32).also { SecureRandom().nextBytes(it) }
                .joinToString("") { "%02x".format(it) }

            return PaymentBridge(token, allowedOrigin, objects)
        }

        private val LEADING_DOCTYPE =
            Regex("^\\uFEFF?(\\s*<!DOCTYPE[^>]*>)?", RegexOption.IGNORE_CASE)
    }
}
