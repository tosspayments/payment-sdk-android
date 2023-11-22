package io.fincube.appview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;

import java.util.Arrays;

import io.fincube.creditcard.Util;
import io.fincube.ocr.OcrConfig;
import io.fincube.ocr.OverlayView;

public class CustomOverlayView extends OverlayView
{
    public CustomOverlayView(Context context, AttributeSet attributeSet, OcrConfig config) {
        super(context, attributeSet, config);

        initMembers();
    }

    private Path mLockedBackgroundPath;
    private Paint mLockedBackgroundPaint;
    private Paint mGuidePaint;
    private int guideColor = 0xFFFFFFFF;

    // parent member variables

    /**
     * getCameraPreviewRect() : return camera preview rect object
     */

    private void initMembers() {
        mGuidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void initOverlayView(Rect guide, Rect cameraPreviewRect, int rotation) {
        mLockedBackgroundPath = new Path();
        mLockedBackgroundPath.setFillType(Path.FillType.EVEN_ODD);
        mLockedBackgroundPath.addRect(new RectF(getCameraPreviewRect()), Path.Direction.CW);

        float radii[] = new float[8];
        Arrays.fill(radii, 80.0f);
        mLockedBackgroundPath.addRoundRect(new RectF(guide), radii, Path.Direction.CW);
        mLockedBackgroundPath.close();

        mLockedBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLockedBackgroundPaint.clearShadowLayer();
        mLockedBackgroundPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDrawCanvas(Canvas canvas, Rect guide, Rect cameraPreviewRect, boolean cardDetected) {
        canvas.save();

        // Draw guide lines
        mGuidePaint.clearShadowLayer();
        mGuidePaint.setStyle(Paint.Style.FILL);
        mGuidePaint.setColor(guideColor);
        mGuidePaint.setTextAlign(Paint.Align.CENTER);


        if( cardDetected )
            mLockedBackgroundPaint.setColor(0xBB000000); // 75% black
        else
            mLockedBackgroundPaint.setColor(0xBBFFFFFF); // 75% white

        canvas.drawPath(mLockedBackgroundPath, mLockedBackgroundPaint);

        if (cardDetected) {
            // Draw guide text
            // Set up paint attributes
            float guideHeight = GUIDE_LINE_HEIGHT * mScale;
            float guideFontSize = GUIDE_FONT_SIZE * mScale;

            Util.setupTextPaintStyle(mGuidePaint);
            mGuidePaint.setTextAlign(Paint.Align.CENTER);
            mGuidePaint.setTextSize(guideFontSize);

            // Translate and rotate text
            canvas.translate(mGuide.left + mGuide.width() / 2, mGuide.top + mGuide.height() / 2);
            //canvas.rotate(mRotationFlip * mRotation);

            if (scanInstructions != null && scanInstructions != "") {
                String[] lines = scanInstructions.split("\n");
                float y = -(((guideHeight * (lines.length - 1)) - guideFontSize) / 2) - 3;

                for (int i = 0; i < lines.length; i++) {
                    canvas.drawText(lines[i], 0, y, mGuidePaint);
                    y += guideHeight;
                }
            }
        }
        else
        {
            canvas.drawText("Put the card at guide rect", canvas.getWidth()/2, canvas.getHeight()/2, mGuidePaint);
        }
        canvas.restore();
    }
}
