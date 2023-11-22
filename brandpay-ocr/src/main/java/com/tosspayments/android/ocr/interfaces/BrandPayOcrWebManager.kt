package com.tosspayments.android.ocr.interfaces

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.tosspayments.android.ocr.activity.BrandPayCardScanActivity
import com.tosspayments.android.ocr.model.ErrorCode
import com.tosspayments.android.ocr.model.OcrError
import java.lang.ref.WeakReference

class BrandPayOcrWebManager(activity: FragmentActivity) {
    companion object {
        const val REQUEST_CODE_CARD_SCAN = 20001
        const val EXTRA_CARD_SCAN_RESULT_SCRIPT = "extraCardScanResultScript"

        private const val JAVASCRIPT_INTERFACE_NAME = "ConnectPayOcr"
    }

    interface Callback {
        fun onPostScript(script: String)
    }

    private val ocrJavaScriptInterface = BrandPayOcrJavascriptInterface(activity)

    var callback: Callback? = null
        set(value) {
            ocrJavaScriptInterface.callback = value
            field = value
        }

    @SuppressLint("JavascriptInterface")
    fun addJavascriptInterface(webView: WebView) {
        webView.addJavascriptInterface(
            ocrJavaScriptInterface,
            JAVASCRIPT_INTERFACE_NAME
        )
    }

    @JvmOverloads
    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent? = null
    ) {
        ocrJavaScriptInterface.handleActivityResult(requestCode, resultCode, data)
    }

    private class BrandPayOcrJavascriptInterface(activity: FragmentActivity) {
        private val gson = Gson()

        private var weakActivity: WeakReference<FragmentActivity> = WeakReference(activity)

        private var onSuccess: String? = null
        private var onError: String? = null

        var callback: Callback? = null

        @JavascriptInterface
        fun postMessage(message: String) {
            weakActivity.get()?.let { activity ->
                kotlin.runCatching {
                    val jsonObj: JsonObject? = Gson().fromJson(message, JsonObject::class.java)
                    val name = jsonObj?.get("name")?.asString
                    val params = jsonObj?.getAsJsonObject("params")
                    onSuccess = params?.get("onSuccess")?.asString
                    onError = params?.get("onError")?.asString

                    when (name) {
                        "scanOCRCard" -> {
                            val license = params?.get("license")?.asString
                            requestCardScan(activity, onSuccess, onError, license)
                        }
                        "isOCRAvailable" -> postOnSuccess(
                            onSuccess,
                            isCameraAvailable(activity).toString()
                        )
                        else -> {
                        }
                    }
                }
            }
        }

        private fun requestCardScan(
            activity: Activity,
            onSuccess: String?,
            onError: String?,
            license: String?
        ) {
            activity.startActivityForResult(
                BrandPayCardScanActivity.getIntent(activity, license, onSuccess, onError),
                REQUEST_CODE_CARD_SCAN
            )
        }

        private fun isCameraAvailable(context: Context): Boolean {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        }

        private fun postOnSuccess(onSuccess: String?, data: String?) {
            postScript("javascript:$onSuccess('${data.orEmpty()}');")
        }

        private fun postOnError(onError: String?, errorCode: ErrorCode, message: String = "") {
            postScript(
                "javascript:$onError('${
                    gson.toJson(
                        OcrError(
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

        fun handleActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent? = null
        ) {
            if (requestCode == REQUEST_CODE_CARD_SCAN) {
                when (resultCode) {
                    Activity.RESULT_CANCELED -> {
                        postOnError(onError, ErrorCode.OCR_CANCELED)
                    }
                    else -> {
                        postScript(data?.getStringExtra(EXTRA_CARD_SCAN_RESULT_SCRIPT))
                    }
                }
            }
        }
    }
}