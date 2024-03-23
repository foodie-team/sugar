package com.github.foodiestudio.sugar.storage.filesystem.media

import android.os.Build
import androidx.annotation.RequiresApi

enum class MediaStoreType {
    Images,
    Video,

    // The Recordings/ directory isn't available on Android 11 (API level 30) and lower.
    Audio,

    @RequiresApi(Build.VERSION_CODES.Q)
    Downloads
}