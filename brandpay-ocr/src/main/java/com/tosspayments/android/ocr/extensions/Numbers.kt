package com.tosspayments.android.ocr.extensions

import android.content.Context
import android.util.TypedValue

fun Number.dipToPx(context: Context): Int {
    return this.toFloat().takeUnless { it > 0f }?.toInt()
        ?: TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        ).toInt().coerceAtLeast(1)
}