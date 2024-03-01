package com.github.foodiestudio.sugar.storage.filesystem.media

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.RequiresApi
import com.github.foodiestudio.sugar.ExperimentalSugarApi
import com.github.foodiestudio.sugar.storage.filesystem.toOkioPath
import okio.BufferedSink
import okio.BufferedSource
import okio.FileMetadata
import okio.FileSystem

// 参考自 https://github.com/anggrayudi/SimpleStorage/blob/master/storage/src/main/java/com/anggrayudi/storage/media/MediaFile.kt
// 暂只考虑 Android 10+ 的情况
@RequiresApi(Build.VERSION_CODES.Q)
@ExperimentalSugarApi
class MediaFile(context: Context, val mediaUri: Uri) {
    internal val resolver = context.contentResolver

    private val fileSystem: FileSystem = MediaFileSystem(context)

    /**
     * 可以进一步获取 displayName、mimeType、absoluteFilePath等
     */
    val metadata: FileMetadata
        get() = fileSystem.metadata(mediaUri.toOkioPath())

    fun loadThumbnail(size: Size): Bitmap {
        return resolver.loadThumbnail(mediaUri, size, null)
    }

    fun renameTo(displayName: String) {
        check(displayName.isNotEmpty())
        MediaStoreUpdateBuilder(resolver).update(mediaUri) {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        }
    }

    fun <T> write(action: BufferedSink.() -> T) {
        fileSystem.write(mediaUri.toOkioPath(), writerAction = action)
    }

    fun <T> read(action: BufferedSource.() -> T) {
        fileSystem.read(mediaUri.toOkioPath(), action)
    }

    fun releasePendingStatus() {
        ContentValues().apply {
            put(MediaStore.Audio.Media.IS_PENDING, 0)
        }.let {
            resolver.update(mediaUri, it, null, null)
        }
    }

    companion object {
        /**
         * @param enablePending 是否允许 pending 状态, 默认为 false, 可以在写入完成后调用 [releasePendingStatus]
         */
        fun create(
            context: Context,
            type: MediaStoreType,
            fileName: String,
            relativePath: String? = null,
            enablePending: Boolean = false
        ): MediaFile = createFile(
            context,
            type,
            relativePath = relativePath,
            fileName = fileName,
            enablePending
        )
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalSugarApi::class)
fun MediaFile.moveTo(newRelativePath: String) {
    MediaStoreUpdateBuilder(resolver).update(mediaUri) {
        put(MediaStore.MediaColumns.RELATIVE_PATH, newRelativePath)
    }
}

@ExperimentalSugarApi
@RequiresApi(Build.VERSION_CODES.Q)
private fun createFile(
    context: Context,
    type: MediaStoreType,
    relativePath: String? = null,
    fileName: String,
    enablePending: Boolean = false,
): MediaFile {
    val collection =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (type) {
                MediaStoreType.Audio -> MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )

                MediaStoreType.Images -> MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )

                MediaStoreType.Video -> MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )

                else -> MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }
        } else {
            when (type) {
                MediaStoreType.Audio -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

                MediaStoreType.Images -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                MediaStoreType.Video -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI

                else -> MediaStore.Downloads.EXTERNAL_CONTENT_URI
            }
        }

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        if (!relativePath.isNullOrEmpty()) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        }
        if (enablePending) {
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }
    val uri = context.contentResolver.insert(collection, contentValues)!!
    return MediaFile(context, uri)
}