package com.tosspayments.paymentsdk.auth.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.tosspayments.paymentsdk.auth.persistences.BrandPayUtilPreference
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

internal object BioMetricUtil {
    private const val KEY_TOKEN = "brandPayAuthKeyToken"

    private enum class BioMetricType {
        FACE, FINGERPRINT
    }

    private lateinit var preference: BrandPayUtilPreference

    fun getBiometricAuthMethods(context: Context): List<String> {
        return when (BiometricManager.from(context)
            .canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val bioMetricTypes = mutableSetOf<BioMetricType>()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
                        bioMetricTypes.add(BioMetricType.FINGERPRINT)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        && context.packageManager.hasSystemFeature(PackageManager.FEATURE_FACE)
                    ) {
                        bioMetricTypes.add(BioMetricType.FACE)
                    }
                } else {
                    bioMetricTypes.add(BioMetricType.FINGERPRINT)
                }

                bioMetricTypes.map { it.name }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                throw Exception("생체인증을 지원하지 않는 기기입니다.")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                throw Exception("등록된 생체인증이 없습니다.")
            }
            else -> {
                throw Exception("생체인증을 사용할 수 없습니다.")
            }
        }
    }

    fun requestAuth(
        activity: FragmentActivity,
        callback: BiometricPrompt.AuthenticationCallback
    ) {
        activity.runOnUiThread {
            BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback)
                .authenticate(
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle("생체 정보로 인증해주세요")
                        .setNegativeButtonText("취소")
                        .build()
                )
        }
    }

    fun hasBioMetricAuth(context: Context): Boolean {
        if (!BioMetricUtil::preference.isInitialized) {
            preference = BrandPayUtilPreference.getInstance(context)
        }

        return !preference.getString(KEY_TOKEN).isNullOrBlank()
    }

    @Throws
    fun getBioMetricAuth(context: Context, modulus: String, exponent: String): String {
        if (!BioMetricUtil::preference.isInitialized) {
            preference = BrandPayUtilPreference.getInstance(context)
        }

        try {
            return rsaEncrypt(preference.getString(KEY_TOKEN).orEmpty(), modulus, exponent)
        } catch (e: Exception) {
            throw e
        }
    }

    fun registerBioMetricAuth(context: Context, token: String?): Boolean {
        if (!BioMetricUtil::preference.isInitialized) {
            preference = BrandPayUtilPreference.getInstance(context)
        }

        return try {
            preference.putString(KEY_TOKEN, token.orEmpty())
            true
        } catch (e: Exception) {
            throw e
        }
    }

    fun unregisterBioMetricAuth(context: Context) {
        if (!BioMetricUtil::preference.isInitialized) {
            preference = BrandPayUtilPreference.getInstance(context)
        }

        try {
            preference.putString(KEY_TOKEN, "")
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getCipher(
        mode: Int,
        modulus: String,
        exponent: String,
        transformation: String,
        algorithm: String
    ): Cipher {
        val cipher: Cipher = Cipher.getInstance(transformation)
        val keySpec = RSAPublicKeySpec(BigInteger(modulus, 16), BigInteger(exponent, 16))
        val publicKey: PublicKey = KeyFactory.getInstance(algorithm).generatePublic(keySpec)

        cipher.init(mode, publicKey)
        return cipher
    }

    private fun getRsaEncryptCipher(
        modulus: String,
        exponent: String
    ): Cipher {
        return getCipher(Cipher.ENCRYPT_MODE, modulus, exponent, "RSA/ECB/PKCS1Padding", "RSA")
    }

    private fun rsaEncrypt(source: String, modulus: String, exponent: String): String {
        val cipher = getRsaEncryptCipher(modulus, exponent)
        return cipher.doFinal(source.toByteArray()).toHexString()
    }

    @ExperimentalUnsignedTypes
    private fun ByteArray.toHexString() =
        asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
}