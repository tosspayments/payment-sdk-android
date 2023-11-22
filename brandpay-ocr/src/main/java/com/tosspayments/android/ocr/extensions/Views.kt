package com.tosspayments.android.ocr.extensions

import android.view.View

internal fun View.setVisibility(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}