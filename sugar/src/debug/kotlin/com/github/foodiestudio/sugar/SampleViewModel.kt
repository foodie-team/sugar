package com.github.foodiestudio.sugar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.github.foodiestudio.sugar.storage.AppFileHelper
import okio.Path
import okio.Path.Companion.toOkioPath

@OptIn(ExperimentalSugarApi::class)
internal class SampleViewModel(application: Application) : AndroidViewModel(application) {

    private val fileHelper = AppFileHelper(application)

    var testVideoPath: Path = fileHelper.requireFilesDir(false).toOkioPath().resolve("video.mp4")

    var testImagePath: Path = fileHelper.requireFilesDir(false).toOkioPath().resolve("image.jpg")

    val isPrepared: Boolean
        get() = fileHelper.fileSystem.run {
            exists(testImagePath) && exists(testVideoPath)
        }

}