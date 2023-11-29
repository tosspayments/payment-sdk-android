package com.tosspayments.android.auth.interfaces

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.tosspayments.android.auth.model.ErrorCode
import com.tosspayments.android.auth.utils.BioMetricUtil
import com.tosspayments.android.auth.utils.BrandPayAuthManager
import java.lang.ref.WeakReference

class BrandPayAuthWebManager(activity: FragmentActivity) {
    companion object {
        private const val JAVASCRIPT_INTERFACE_NAME = "ConnectPayAuth"
    }

    interface Callback {
        fun onPostScript(script: String)
    }

    private val authJavascriptInterface = BrandPayAuthJavascriptInterface(activity)

    var callback: Callback? = null
        set(value) {
            authJavascriptInterface.callback = value
            field = value
        }

    @SuppressLint("JavascriptInterface")
    fun addJavascriptInterface(webView: WebView) {
        webView.addJavascriptInterface(
            authJavascriptInterface,
            JAVASCRIPT_INTERFACE_NAME
        )
    }

    private class BrandPayAuthJavascriptInterface(activity: FragmentActivity) {
        private val gson = Gson()

        private var weakActivity: WeakReference<FragmentActivity> = WeakReference(activity)

        var callback: Callback? = null

        @JavascriptInterface
        fun postMessage(message: String) {
            weakActivity.get()?.let { activity ->
                kotlin.runCatching {
                    val jsonObj: JsonObject? = Gson().fromJson(message, JsonObject::class.java)
                    val name = jsonObj?.get("name")?.asString
                    val params: JsonObject? = jsonObj?.getAsJsonObject("params")
                    val onSuccess = params?.get("onSuccess")?.asString
                    val onError = params?.get("onError")?.asString

                    when (name) {
                        "getAppInfo" -> getAppInfo(activity, onSuccess)
                        "getBiometricAuthMethods" -> {
                            getBiometricAuthMethods(activity, onSuccess)
                        }
                        "verifyBiometricAuth" -> {
                            verifyBiometricAuth(
                                activity,
                                params?.get("modulus")?.asString.orEmpty(),
                                params?.get("exponent")?.asString.orEmpty(),
                                onSuccess,
                                onError
                            )
                        }
                        "hasBiometricAuth" -> {
                            hasBiometricAuth(activity, onSuccess)
                        }
                        "registerBiometricAuth" -> {
                            registerBiometricAuth(
                                activity,
                                params?.get("biometricToken")?.asString.orEmpty(),
                                onSuccess,
                                onError
                            )
                        }
                        "unregisterBiometricAuth" -> {
                            unregisterBiometricAuth(
                                activity,
                                onSuccess,
                                onError
                            )
                        }
                        else -> {
                        }
                    }
                }
            }
        }

        private fun getAppInfo(activity: FragmentActivity, onSuccess: String?) {
            val appInfo = try {
                gson.toJson(BrandPayAuthManager.getAppInfo(activity))
            } catch (e: Exception) {
                ""
            }

            postOnSuccess(onSuccess, appInfo)
        }

        private fun getBiometricAuthMethods(
            activity: FragmentActivity,
            onSuccess: String?
        ) {
            val methods = BrandPayAuthManager.getBiometricAuthMethods(activity)

            if (methods.isEmpty()) {
                postOnSuccess(onSuccess, "")
            } else {
                postOnSuccess(onSuccess, methods)
            }
        }

        private fun verifyBiometricAuth(
            activity: FragmentActivity,
            modulus: String,
            exponent: String,
            onSuccess: String?,
            onError: String?
        ) {
            BrandPayAuthManager.requestBioMetricAuth(activity,
                modulus,
                exponent,
                {
                    postOnSuccess(onSuccess, it)
                },
                { code, message ->
                    postOnError(onError, code, message)
                })
        }

        private fun hasBiometricAuth(
            activity: FragmentActivity,
            onSuccess: String?
        ) {
            postOnSuccess(
                onSuccess,
                BrandPayAuthManager.hasBioMetricAuth(activity).toString()
            )
        }

        private fun registerBiometricAuth(
            activity: FragmentActivity,
            token: String,
            onSuccess: String?,
            onError: String?
        ) {
            if (token.isBlank()) {
                postOnError(onError, ErrorCode.BIOMETRIC_INVALID, "Token을 확인해주세요.")
            } else {
                BioMetricUtil.requestAuth(activity,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            when (errorCode) {
                                BiometricPrompt.ERROR_NEGATIVE_BUTTON, BiometricPrompt.ERROR_USER_CANCELED -> {
                                    postOnError(onError, ErrorCode.BIOMETRIC_CANCELED)
                                }
                                else -> {}
                            }
                        }

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            BrandPayAuthManager.registerBiometricAuth(activity, token,
                                {
                                    postOnSuccess(onSuccess, true.toString())
                                }, { code, message ->
                                    postOnError(onError, code, message)
                                })
                        }
                    })
            }
        }

        private fun unregisterBiometricAuth(
            activity: FragmentActivity,
            onSuccess: String?,
            onError: String?
        ) {
            BrandPayAuthManager.unregisterBiometricAuth(activity,
                {
                    postOnSuccess(onSuccess, "")
                },
                { code, message ->
                    postOnError(onError, code, message)
                })
        }

        private fun postOnSuccess(onSuccess: String?, data: String?) {
            postScript("javascript:$onSuccess('${data.orEmpty()}');")
        }

        private fun postOnError(onError: String?, errorCode: ErrorCode, message: String = "") {
            postScript(
                "javascript:$onError('${
                    gson.toJson(
                        AuthError(
                            errorCode.name,
                            "${errorCode.message}$message"
                        )
                    )
                }');"
            )
        }

        private fun postScript(script: String?) {
            weakActivity.get()?.runOnUiThread {
                callback?.onPostScript(script.orEmpty())
            }
        }
    }

    private class AuthError(
        @SerializedName("code")
        val code: String,
        @SerializedName("message")
        val message: String
    )
}