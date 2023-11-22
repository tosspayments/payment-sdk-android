package com.tosspayments.android.ocr.common

import androidx.fragment.app.FragmentActivity
import com.tosspayments.android.ocr.activity.BrandPayCardScanActivity

object BrandPayOcrManager {
    @JvmStatic
    @JvmOverloads
    fun requestCardScan(
        activity: FragmentActivity,
        license: String?,
        resultCode: Int? = null
    ) {
        val intent = BrandPayCardScanActivity.getIntent(activity, license)

        resultCode?.let { activity.startActivityForResult(intent, it) }
            ?: activity.startActivity(intent)
    }
}