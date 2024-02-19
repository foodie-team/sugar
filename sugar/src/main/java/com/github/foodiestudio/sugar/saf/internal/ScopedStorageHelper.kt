package com.github.foodiestudio.sugar.saf.internal

import android.os.Build
import androidx.annotation.RequiresApi

/**
 * 从 Android 10(API level 29) 起，引入了 ScopedStorage，不过这是第一个版本，作为过渡，可以使用 preserveLegacyExternalStorage
 * 来不开启，但到 Android 11 起，新安装的应用将无视这个字段，也就是说，要做适配要在 Android 10 这个版本去做。
 *
 * 在当下这个时间点（Android 14 已经出了），几乎所有的 App 都不会用到 preserveLegacyExternalStorage，
 * 可以理解为 Android 10 的设备上肯定是开启 ScopedStorage
 *
 */

@RequiresApi(Build.VERSION_CODES.Q)
object ScopedStorageHelper {

}