package com.github.foodiestudio.sugar

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import com.github.foodiestudio.sugar.storage.AppFileHelper
import com.github.foodiestudio.sugar.storage.filesystem.FileSystemCompat
import com.github.foodiestudio.sugar.storage.filesystem.media.MediaFile
import com.github.foodiestudio.sugar.storage.filesystem.media.MediaStoreType
import com.github.foodiestudio.sugar.storage.filesystem.media.moveTo
import com.github.foodiestudio.sugar.storage.filesystem.toOkioPath
import okio.FileMetadata
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

    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportVideo(context: Context): Uri {
        val source = FileSystemCompat(context).source(testVideoPath)
        return MediaFile.create(
            context,
            MediaStoreType.Video,
            relativePath = "Movies/sugar",
            fileName = "video.mp4",
            enablePending = true
        ).let {
            it.write {
                writeAll(source)
            }
            it.releasePendingStatus()
            it.mediaUri
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportImage(context: Context): Uri {
        val source = FileSystemCompat(context).source(testImagePath)
        return MediaFile.create(
            context,
            MediaStoreType.Images,
            relativePath = "Pictures/sugar",
            fileName = "img.png", // 如果本身已经存在这个名字的文件，会自动补上(1)这类数字后缀
            enablePending = true
        ).let {
            it.write {
                writeAll(source)
            }
            it.releasePendingStatus()
            it.mediaUri
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun readImageMetadata(context: Context, exportedImageUri: Uri): FileMetadata {
        return MediaFile(context, exportedImageUri).metadata
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun moveImage(context: Context, exportedImageUri: Uri, newRelativePath: String) {
        MediaFile(context, exportedImageUri).moveTo(newRelativePath)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun renameImage(context: Context, exportedImageUri: Uri, name: String) {
        MediaFile(context, exportedImageUri).renameTo(displayName = name)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun loadThumbnail(context: Context, exportedVideoUri: Uri): Bitmap {
        return MediaFile(context, exportedVideoUri).loadThumbnail(Size(256, 256))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveTextToDownload(context: Context, content: String): Uri {
        return MediaFile.create(
            context,
            MediaStoreType.Downloads,
            relativePath = "Download/sugar",
            fileName = "demo.txt", // 如果本身已经存在这个名字的文件，会自动补上(1)这类数字后缀
            enablePending = true
        ).let {
            it.write {
                writeUtf8(content)
            }
            it.releasePendingStatus()
            it.mediaUri
        }
    }
}