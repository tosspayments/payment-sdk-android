package com.tosspayments.paymentsdk.extension

import android.content.Context
import android.util.DisplayMetrics

fun Float.toDp(context: Context): Int {
    val metrics: DisplayMetrics = context.resources.displayMetrics
    return (this / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
}