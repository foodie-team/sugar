import android.content.Context
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.StringRes
import me.drakeet.support.toast.ToastCompat

/**
 * 一个 App 内的所定义的 ToastStyle 不应该有太多种，如果有的话，应该检查下设计的合理性。
 * 基于这一点，只需要定义少数的 Style 就够了，不需要用 Builder 的方式去描述，后者更适合需要经常自定义的场景。
 */
// TODO: 对 Toast 更多的属性支持
data class ToastStyle(
    val gravity: Int = Gravity.NO_GRAVITY,
    val xOffset: Int = 0,
    val yOffset: Int = 0
)

object ToastStyles {
    val Default = ToastStyle()
}

fun Context?.toast(@StringRes message: Int, style: ToastStyle = ToastStyles.Default) {
    this?.toast(getString(message), style)
}

fun Context?.toast(message: CharSequence?, style: ToastStyle = ToastStyles.Default) {
    if (message != null && this != null) {
        buildToast(this, message, style, false).show()
    }
}

/**
 * [toast] 的 long 版本
 */
fun Context?.longToast(@StringRes message: Int, style: ToastStyle = ToastStyles.Default) {
    this?.longToast(getString(message), style)
}

/**
 * [toast] 的 long 版本
 */
fun Context?.longToast(message: CharSequence?, style: ToastStyle = ToastStyles.Default) {
    if (message != null && this != null) {
        buildToast(this, message, style, true).show()
    }
}

private fun buildToast(
    context: Context,
    message: CharSequence,
    style: ToastStyle,
    longToast: Boolean
): Toast {
    return ToastCompat.makeText(
        context,
        message,
        if (longToast) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    ).apply {
        setGravity(style.gravity, style.xOffset, style.yOffset)
    }
}