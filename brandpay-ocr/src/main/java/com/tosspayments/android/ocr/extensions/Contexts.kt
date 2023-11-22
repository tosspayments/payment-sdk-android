package com.tosspayments.android.ocr.extensions

import android.content.Context
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tosspayments.android.ocr.R

internal fun Context.showDialog(
    title: String = "",
    message: String = "",
    positiveButton: Pair<String, DialogInterface.OnClickListener>? = null,
    negativeButton: Pair<String, DialogInterface.OnClickListener>? = null,
    dismissListener: DialogInterface.OnDismissListener? = null
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .apply {
            positiveButton?.let {
                setPositiveButton(it.first, it.second)
            } ?: kotlin.run {
                setPositiveButton(
                    getString(R.string.confirm)
                ) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
            }

            negativeButton?.let {
                setNegativeButton(it.first, it.second)
            }
        }
        .setOnDismissListener(dismissListener)
        .show()
}