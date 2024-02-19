package com.github.foodiestudio.sugar.saf.ext

import android.content.Context
import android.os.Environment
import com.github.foodiestudio.sugar.ExperimentalSugarApi
import java.io.File

/**
 * 仅在当前应用用到的一些数据
 */
@ExperimentalSugarApi
class AppOwnFilesHelper(context: Context) {

    val privateCacheDir: File by lazy {
        context.cacheDir
    }

    val privateFilesDir: File by lazy {
        context.filesDir
    }

    val publicCacheDir: File by lazy {
        context.externalCacheDir!!
    }

    val publicCacheDirs: Array<File> by lazy {
        context.externalCacheDirs
    }

    val publicFilesDir: File by lazy {
        context.getExternalFilesDir(null)!!
    }

    val publicMoviesDir: File by lazy {
        context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)!!
    }

    // 相机拍摄存放的位置
    val publicDCIMDir: File by lazy {
        context.getExternalFilesDir(Environment.DIRECTORY_DCIM)!!
    }

}