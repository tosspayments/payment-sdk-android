package com.tosspayments.paymentsdk.extension

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URISyntaxException

fun Context.startSchemeIntent(url: String): Boolean {
    val schemeIntent: Intent = try {
        Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
    } catch (e: URISyntaxException) {
        return false
    }

    try {
        startActivity(schemeIntent)
        return true
    } catch (e: ActivityNotFoundException) {
        val packageName = schemeIntent.getPackage()

        if (!packageName.isNullOrBlank()) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$packageName")
                )
            )
            return true
        }
    }
    return false
}