package com.tosspayments.paymentsdk.auth.persistences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys

internal class BrandPayUtilPreference private constructor(context: Context) {
    private val preference: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "tossBrandPayUtilPreference",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MasterKey.Builder(context)
                .setKeyGenParameterSpec(MasterKeys.AES256_GCM_SPEC)
                .build()
        } else {
            MasterKey.Builder(context).build()
        },
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private lateinit var instance: BrandPayUtilPreference

        fun getInstance(context: Context): BrandPayUtilPreference {
            return if (Companion::instance.isInitialized) {
                instance
            } else {
                BrandPayUtilPreference(context).also {
                    instance = it
                }
            }
        }
    }

    @SuppressLint("CommitPrefEdits")
    fun putString(key: String, value: String) {
        preference.edit {
            putString(key, value)
        }
    }

    fun getString(key: String): String? {
        return preference.getString(key, null)
    }
}