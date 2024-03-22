package com.github.foodiestudio.sugar.notification

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context?.toast(@StringRes message: Int) {
    this?.toast(getString(message))
}

fun Context?.toast(message: CharSequence?) {
    if (message != null && this != null) {
        buildToast(this, message, false).show()
    }
}

/**
 * [toast] 的 long 版本
 */
fun Context?.longToast(@StringRes message: Int) {
    this?.longToast(getString(message))
}

/**
 * [toast] 的 long 版本
 */
fun Context?.longToast(message: CharSequence?) {
    if (message != null && this != null) {
        buildToast(this, message, true).show()
    }
}

/**
 * 内容先置为 null 再设置实际文本，可以规避一些机型 toast 前缀强制带上「应用名」
 */
private fun buildToast(
    context: Context,
    message: CharSequence,
    longToast: Boolean
): Toast {
    return Toast.makeText(
        context,
        "",
        if (longToast) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    ).apply {
        setText(message)
    }
}