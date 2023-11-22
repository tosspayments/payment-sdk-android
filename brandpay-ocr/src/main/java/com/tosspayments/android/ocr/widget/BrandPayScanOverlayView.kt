package com.tosspayments.android.ocr.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.tosspayments.android.ocr.extensions.dipToPx
import io.fincube.ocr.OcrConfig
import io.fincube.ocr.OverlayView
import java.util.*

@SuppressLint("ViewConstructor")
class BrandPayScanOverlayView(
    context: Context,
    attributeSet: AttributeSet? = null,
    config: OcrConfig? = null
) :
    OverlayView(context, attributeSet, config) {
    private lateinit var mLockedBackgroundPath: Path
    private lateinit var mLockedBackgroundPaint: Paint
    private lateinit var mGuidePaint: Paint
    private val mRectF = RectF()

    init {
        initMembers()
    }

    private fun initMembers() {
        mGuidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 12f
            strokeCap = Paint.Cap.ROUND
        }
    }

    override fun initOverlayView(guide: Rect, cameraPreviewRect: Rect, rotation: Int) {
        val radius = FloatArray(8)
        Arrays.fill(radius, 12f.dipToPx(context).toFloat())

        mLockedBackgroundPath = Path().apply {
            fillType = Path.FillType.EVEN_ODD
            addRect(RectF(getCameraPreviewRect()), Path.Direction.CW)
            addRoundRect(RectF(guide), radius, Path.Direction.CW)
        }

        mLockedBackgroundPath.close()

        mLockedBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            clearShadowLayer()
            style = Paint.Style.FILL
            color = Color.parseColor("#bf000000")
        }

        mRectF.set(
            mGuide.left.toFloat(),
            mGuide.top.toFloat(),
            mGuide.right.toFloat(),
            mGuide.bottom.toFloat()
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    override fun onDrawCanvas(
        canvas: Canvas,
        guide: Rect,
        cameraPreviewRect: Rect,
        cardDetected: Boolean
    ) {
        canvas.save()

        mGuidePaint.run {
            clearShadowLayer()
            color = if (!cardDetected) Color.parseColor("#fafafc") else Color.parseColor("#04fd9d")
        }

        canvas.drawPath(mLockedBackgroundPath, mLockedBackgroundPaint)
        canvas.drawRoundRect(
            mRectF,
            12f.dipToPx(context).toFloat(),
            12f.dipToPx(context).toFloat(),
            mGuidePaint
        )

        canvas.restore()
    }
}