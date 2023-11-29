package com.tosspayments.android.auth.utils

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.tosspayments.android.auth.model.AppInfo
import com.tosspayments.android.auth.model.ErrorCode

object BrandPayAuthManager {
    @JvmStatic
    fun getAppInfo(context: Context): AppInfo {
        return AppInfo(context.packageName)
    }

    @JvmStatic
    @JvmOverloads
    fun requestBioMetricAuth(
        activity: FragmentActivity,
        modulus: String,
        exponent: String,
        onSuccess: ((String) -> Unit)? = null,
        onError: ((ErrorCode, String) -> Unit)? = null
    ) {
        fun invokeOnSuccess(data: String) {
            activity.runOnUiThread {
                onSuccess?.invoke(data)
            }
        }

        fun invokeOnError(errorCode: ErrorCode, message: String = "") {
            activity.runOnUiThread {
                onError?.invoke(errorCode, "${errorCode.message}$message")
            }
        }

        try {
            BioMetricUtil.getBiometricAuthMethods(activity)
        } catch (e: Exception) {
            invokeOnError(ErrorCode.BIOMETRIC_FAILED, e.message.orEmpty())
            return
        }

        if (!BioMetricUtil.hasBioMetricAuth(activity)) {
            invokeOnError(ErrorCode.BIOMETRIC_INVALID, "생체인증이 설정되지 않았습니다.")
            return
        }

        BioMetricUtil.requestAuth(activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON, BiometricPrompt.ERROR_USER_CANCELED -> {
                            invokeOnError(ErrorCode.BIOMETRIC_CANCELED)
                        }
                        else -> {}
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    try {
                        invokeOnSuccess(
                            BioMetricUtil.getBioMetricAuth(
                                activity,
                                modulus,
                                exponent
                            )
                        )
                    } catch (e: Exception) {
                        invokeOnError(ErrorCode.BIOMETRIC_FAILED, e.message.orEmpty())
                    }
                }
            })
    }

    @JvmStatic
    fun getBiometricAuthMethods(activity: FragmentActivity): String {
        return Gson().toJson(
            try {
                BioMetricUtil.getBiometricAuthMethods(activity)
            } catch (e: Exception) {
                emptyList<String>()
            }
        )
    }

    @JvmStatic
    fun hasBioMetricAuth(activity: FragmentActivity): Boolean {
        return BioMetricUtil.hasBioMetricAuth(activity)
    }

    @JvmStatic
    fun registerBiometricAuth(
        activity: FragmentActivity,
        token: String?,
        onSuccess: (() -> Unit),
        onError: ((ErrorCode, String) -> Unit)
    ) {
        kotlin.runCatching {

            BioMetricUtil.registerBioMetricAuth(
                activity,

                token.orEmpty()
            )
        }.onSuccess {
            activity.runOnUiThread {
                onSuccess.invoke()
            }
        }.onFailure {
            activity.runOnUiThread {
                onError.invoke(ErrorCode.BIOMETRIC_FAILED, it.message.orEmpty())
            }
        }
    }

    @JvmStatic
    fun unregisterBiometricAuth(
        activity: FragmentActivity,
        onSuccess: (() -> Unit),
        onError: ((ErrorCode, String) -> Unit)
    ) {
        kotlin.runCatching {
            BioMetricUtil.unregisterBioMetricAuth(activity)
        }.onSuccess {
            activity.runOnUiThread {
                onSuccess.invoke()
            }
        }.onFailure {
            activity.runOnUiThread {
                onError.invoke(ErrorCode.BIOMETRIC_FAILED, it.message.orEmpty())
            }
        }
    }
}