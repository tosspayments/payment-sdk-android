package com.tosspayments.android.auth.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.ResultReceiver
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.postDelayed
import kotlin.math.roundToInt

object CommonUtils {
    private const val DEFAULT_SOFT_INPUT_DELAY = 350L

    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun getAppHeight(context: Context): Int {
        return getScreenHeight(context) - getStatusBarHeight(context)
    }

    fun convertDipToPx(dip: Number, context: Context): Int {
        return dip.toFloat().takeUnless { it > 0f }?.toInt()
            ?: TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip.toFloat(),
                context.resources.displayMetrics
            ).toInt().coerceAtLeast(1)
    }

    fun convertSpToPx(sp: Number, context: Context): Int {
        return sp.toFloat().takeUnless { it > 0f }?.toInt()
            ?: TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp.toFloat(),
                context.resources.displayMetrics
            ).toInt().coerceAtLeast(1)
    }

    fun convertPxToDip(px: Number, context: Context): Float {
        return px.toFloat() / context.resources.displayMetrics.density
    }

    fun convertPxToSp(px: Number, context: Context): Int {
        return (px.toFloat() / context.resources.displayMetrics.scaledDensity).toInt()
    }

    fun convertPercentToHex(percent: Int) = (0xff * percent / 100f).roundToInt()

    fun tint(icon: Drawable, color: Int, mode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN): Drawable {
        val drawable = DrawableCompat.wrap(icon).mutate()
        DrawableCompat.setTintList(drawable, ColorStateList.valueOf(color))
        DrawableCompat.setTintMode(drawable, mode)
        return drawable
    }

    fun tint(
        icon: Drawable,
        color: ColorStateList?,
        mode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN
    ): Drawable {
        val drawable = DrawableCompat.wrap(icon).mutate()
        DrawableCompat.setTintList(drawable, color)
        DrawableCompat.setTintMode(drawable, mode)
        return drawable
    }

    fun hideSoftInputAndClearFocus(editText: EditText?) {
        hideSoftInput(editText)
        editText?.clearFocus()
    }

    fun hideSoftInputAndClearFocus(vararg editTexts: EditText?) {
        for (editText in editTexts) hideSoftInputAndClearFocus(editText)
    }

    fun hideSoftInput(view: View?) {
        if (view == null) {
            return
        }
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * @see [im.toss.uikit.widget.dialog.NeatBottomSheetDialog] dismiss animation 250ms + 100ms (buffer)을 기준으로 기본값 설정
     */
    fun showSoftInputWithDelay(view: View?, delayMillis: Long = DEFAULT_SOFT_INPUT_DELAY) {
        if (view == null) {
            return
        }
        view.postDelayed(delayMillis) { showSoftInput(view) }
    }

    fun showSoftInput(view: View?) {
        if (view == null) {
            return
        }

        view.requestFocus()
        val imm: InputMethodManager = view.context.getSystemService() ?: return
        val targetView = view.findFocus() ?: view

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            targetView.doOnWindowFocused {
                imm.showSoftInput(targetView, 0)
                if (targetView is EditText) {
                    targetView.setSelection(targetView.length())
                }
            }
            return
        }

        try {
            // showSoftInputUnchecked is blocked from Android 10
            // https://developer.android.com/about/versions/10/non-sdk-q#new-blocked
            val showSoftInputUnchecked = InputMethodManager::class.java.getMethod(
                "showSoftInputUnchecked",
                Int::class.javaPrimitiveType,
                ResultReceiver::class.java
            )
            showSoftInputUnchecked.isAccessible = true
            showSoftInputUnchecked.invoke(imm, 0, null)
        } catch (e: Exception) {
            imm.showSoftInput(targetView, 0)
        } finally {
            if (targetView is EditText) {
                targetView.setSelection(targetView.length())
            }
        }
    }

    private inline fun View.doOnWindowFocused(crossinline action: (view: View) -> Unit) {
        if (hasWindowFocus()) {
            action(this)
        } else {
            viewTreeObserver.addOnWindowFocusChangeListener(object :
                ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    if (hasFocus) {
                        viewTreeObserver.removeOnWindowFocusChangeListener(this)
                        action(this@doOnWindowFocused)
                    }
                }
            })
        }
    }
}