package com.tosspayments.paymentsdk.interfaces

import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

internal class BridgeObject(
    val name: String,
    val methods: Map<String, (List<String?>) -> Unit>
)

internal class PaymentBridge(
    private val token: String,
    private val allowedOrigin: BridgeOrigin,
    private val objects: List<BridgeObject>
) {
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

        if (origin != null && !allowedOrigin.allowsMessage(fromMainFrame = true, origin = origin)) {
            return
        }

        val handler = objects.firstOrNull { it.name == target }?.methods?.get(method) ?: return
        val values = runCatching { JSONArray(arguments.orEmpty()) }.getOrNull() ?: return

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

        private val LEADING_DOCTYPE =
            Regex("^\\uFEFF?(\\s*<!DOCTYPE[^>]*>)?", RegexOption.IGNORE_CASE)
    }
}
