package com.github.foodiestudio.sugar

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.github.foodiestudio.sugar.storage.AppFileHelper
import com.github.foodiestudio.sugar.storage.filesystem.toOkioPath
import okio.Path
import okio.Path.Companion.toOkioPath

@OptIn(ExperimentalSugarApi::class)
internal class SampleViewModel(application: Application) : AndroidViewModel(application) {

    private val fileHelper = AppFileHelper(application)

    val testVideoPath: Path = fileHelper.requireFilesDir(false).toOkioPath().resolve("video.mp4")

    val testImagePath: Path = fileHelper.requireFilesDir(false).toOkioPath().resolve("image.jpg")

    val isImagePrepared: Boolean
        get() = fileHelper.fileSystem.run {
            exists(testImagePath)
        }

    val isVideoPrepared: Boolean
        get() = fileHelper.fileSystem.run {
            exists(testVideoPath)
        }

    fun updateImagePath(uri: Uri) {
        fileHelper.fileSystem.copy(uri.toOkioPath(), testImagePath)
    }

    fun updateVideoPath(uri: Uri) {
        fileHelper.fileSystem.copy(uri.toOkioPath(), testVideoPath)
    }

    fun getMetadata(): String {
        TODO()
    }

}