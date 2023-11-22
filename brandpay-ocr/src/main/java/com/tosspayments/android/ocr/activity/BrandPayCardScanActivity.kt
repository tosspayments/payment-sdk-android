package com.tosspayments.android.ocr.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.tosspayments.android.ocr.R
import com.tosspayments.android.ocr.extensions.setVisibility
import com.tosspayments.android.ocr.interfaces.BrandPayOcrWebManager
import com.tosspayments.android.ocr.model.BrandPayCardScanResult
import com.tosspayments.android.ocr.model.ErrorCode
import com.tosspayments.android.ocr.model.OcrError
import com.tosspayments.android.ocr.widget.BrandPayScanOverlayView
import io.fincube.creditcard.DetectionInfo
import io.fincube.ocr.OcrConfig
import io.fincube.ocr.OcrScanner
import io.fincube.ocr.listener.OcrScannerListener
import io.fincube.ocrsdk.OcrConfigSDK
import java.text.DecimalFormat

internal class BrandPayCardScanActivity : Activity() {
    private val gson = Gson()

    private var mOcrConfig: OcrConfig? = null
    private var mOCRScanner: OcrScanner? = null
    private var mScanLayout: FrameLayout? = null

    private var mOnSuccessCallback: String = ""
    private var mOnErrorCallback: String = ""

    companion object {
        private const val REQ_PERMISSION_CAMERA = 1001

        private const val EXTRA_KEY_LICENSE = "extraKeyLicense"
        private const val EXTRA_KEY_ON_SUCCESS = "extraKeyOnSuccess"
        private const val EXTRA_KEY_ON_ERROR = "extraKeyOnError"

        const val EXTRA_KEY_CARD_SCAN_RESULT = "extraKeyCardScanResult"

        fun getIntent(
            activity: Activity,
            license: String? = null,
            onSuccess: String? = null,
            onError: String? = null
        ): Intent {
            return Intent(activity, BrandPayCardScanActivity::class.java).apply {
                putExtra(EXTRA_KEY_LICENSE, license.orEmpty())
                putExtra(EXTRA_KEY_ON_SUCCESS, onSuccess.orEmpty())
                putExtra(EXTRA_KEY_ON_ERROR, onError.orEmpty())
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }

        setContentView(R.layout.activity_brandpay_card_scan)

        mOnSuccessCallback = intent?.getStringExtra(EXTRA_KEY_ON_SUCCESS).orEmpty()
        mOnErrorCallback = intent?.getStringExtra(EXTRA_KEY_ON_ERROR).orEmpty()

        initViews(intent?.getStringExtra(EXTRA_KEY_LICENSE)?.takeIf { it.isNotBlank() })

        checkPermission()
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (checkSelfPermission(Manifest.permission.CAMERA)) {
                PackageManager.PERMISSION_GRANTED -> {
                    setPreviewLayout()
                }
                else -> {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), REQ_PERMISSION_CAMERA)
                }
            }
        } else {
            setPreviewLayout()
        }
    }

    private fun initViews(license: String?) {
        mOcrConfig = OcrConfig().apply {
            licenseKeyFile = license.takeIf { !it.isNullOrBlank() } ?: "fincube_license.flk"

            scannerType = OcrConfigSDK.ScannerType.CREDITCARD.value
            scanExpiry = true
            validateNumber = true
            validateExpiry = true
            cameraIdx = OcrConfig.USE_BACK_CAMERA
            changeOverlayColor = true

            cameraPreviewWidth = resources.displayMetrics.heightPixels
            cameraPreviewHeight = resources.displayMetrics.widthPixels
        }

        mScanLayout = findViewById(R.id.scanLayout)
        findViewById<TextView>(R.id.inputManualCardInfo).setVisibility(false)
    }

    private fun showPermissionAlertDialog() {
        AlertDialog.Builder(this)
            .setMessage("OCR 실행을 위해서는 카메라 권한이 필요합니다.\n권한 허용 후 다시 실행해주십시오.")
            .setOnDismissListener {
                postError(ErrorCode.OCR_INVALID)
            }
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQ_PERMISSION_CAMERA) {
            if (grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED) {
                setPreviewLayout()
            } else {
                showPermissionAlertDialog()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        setPreviewLayout()
    }

    private fun setPreviewLayout() {
        val context = this@BrandPayCardScanActivity

        mOCRScanner = OcrScanner(context)

        if (mOcrConfig != null) {
            mOCRScanner?.run {
                val overlayView = BrandPayScanOverlayView(context, config = mOcrConfig)

                setOcrScannerListener(object : OcrScannerListener {
                    @SuppressLint("CheckResult")
                    override fun onFailure(error_code: Int) {
                        val message: String = when (error_code) {
                            OcrConfigSDK.ERR_CODE_EXPIRED -> "라이센스가 만료되었습니다."
                            OcrConfigSDK.ERR_CODE_INVALID_PACKAGE -> "유효하지 않은 패키지입니다."
                            OcrConfigSDK.ERR_CODE_FAILED_TO_LOAD_DATA -> "데이터 로드에 실패하였습니다."
                            else -> ""
                        }

                        postError(ErrorCode.OCR_FAILED, message)
                    }

                    @SuppressLint("CheckResult")
                    override fun onCardDetected(dinfo: DetectionInfo) {
                        handleScanResult(dinfo)
                    }

                    override fun onCardScannerReady() {
                    }

                    override fun onCardScannerInit() {
                    }
                })

                initView(this@BrandPayCardScanActivity, mOcrConfig, overlayView)
                changeGuideRect(0.5f, 0.5f, 1.0f, 0)

                this.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )

                mScanLayout?.let {
                    it.removeAllViews()
                    it.addView(this)
                }

                startScan()
            }
        }
    }

    private fun handleScanResult(scanResult: DetectionInfo) {
        val cardNo = scanResult.cardNumber.replace("\\s".toRegex(), "")

        val cardNo1 = kotlin.runCatching {
            cardNo.substring(0, 4)
        }.getOrNull().orEmpty()

        val cardNo2 = kotlin.runCatching {
            cardNo.substring(4, 8)
        }.getOrNull().orEmpty()

        val cardNo3 = kotlin.runCatching {
            cardNo.substring(8, 12)
        }.getOrNull().orEmpty()

        val cardNo4 = kotlin.runCatching {
            cardNo.substring(12, cardNo.length)
        }.getOrNull().orEmpty()

        val result = Gson().toJson(
            BrandPayCardScanResult(
                cardNo1,
                cardNo2,
                cardNo3,
                cardNo4,
                try {
                    DecimalFormat("00").format(scanResult.expiry_month) +
                            scanResult.expiry_year.toString().substring(2, 4)
                } catch (e: Exception) {
                    null
                }
            )
        )

        setResult(
            RESULT_OK,
            Intent().apply {
                putExtra(
                    EXTRA_KEY_CARD_SCAN_RESULT,
                    result
                )

                putExtra(
                    BrandPayOcrWebManager.EXTRA_CARD_SCAN_RESULT_SCRIPT,
                    "javascript:$mOnSuccessCallback('${result}');"
                )
            }
        )

        finish()
    }

    private fun postError(code: ErrorCode, message: String = "") {
        setResult(
            RESULT_FIRST_USER, Intent().putExtra(
                BrandPayOcrWebManager.EXTRA_CARD_SCAN_RESULT_SCRIPT,
                "javascript:$mOnErrorCallback('${
                    gson.toJson(
                        OcrError(
                            code.name,
                            "${code.message}$message"
                        )
                    )
                }');"
            )
        )
        finish()
    }

    override fun onPause() {
        super.onPause()

        kotlin.runCatching {
            mOCRScanner?.pauseScan()
        }
    }

    override fun onDestroy() {
        kotlin.runCatching {
            mOCRScanner?.endScan()
        }

        super.onDestroy()
    }
}