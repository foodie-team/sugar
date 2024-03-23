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

/**
 * 仅推荐 Android 10+ 的情况下使用
 *
 * @param mediaUri 主要里面记录了一个 id，修改路径以及 displayName 并不会影响这个 uri， 类似于 content://media/external_primary/images/media/1000060604
 */
@ExperimentalSugarApi
class MediaFile(context: Context, val mediaUri: Uri) {
    init {
        check(mediaUri.authority == MediaStore.AUTHORITY) {
            "mediaUri authority must be ${MediaStore.AUTHORITY}, invalid uri: $mediaUri"
        }
    }

    internal val resolver = context.contentResolver

    private val fileSystem: FileSystem = MediaFileSystem(context)

    /**
     * 可以进一步获取 displayName、mimeType、absoluteFilePath等
     */
    val metadata: FileMetadata
        get() = fileSystem.metadata(mediaUri.toOkioPath())

    /**
     * 仅适用于视频
     */
    @RequiresApi(Build.VERSION_CODES.Q)
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

    @RequiresApi(Build.VERSION_CODES.Q)
    fun releasePendingStatus() {
        ContentValues().apply {
            put(MediaStore.MediaColumns.IS_PENDING, 0)
        }.let {
            resolver.update(mediaUri, it, null, null)
        }
    }

    companion object {
        /**
         * @param enablePending 是否允许 pending 状态, 默认为 false, 可以在写入完成后调用 [releasePendingStatus], 适用于对大文件的读写
         * @param relativePath 这个只能接受对应系统媒体文件夹开头的路径，例如：Movies/XX, Pictures/XX, Downloads/XX
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

/**
 * @param newRelativePath 这个只能接受对应媒体文件夹开头的路径，例如：Movies/XX, Pictures/XX, Downloads/XX
 */
@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalSugarApi::class)
fun MediaFile.moveTo(newRelativePath: String) {
    MediaStoreUpdateBuilder(resolver).update(mediaUri) {
        put(MediaStore.MediaColumns.RELATIVE_PATH, newRelativePath)
    }
}

/**
 * @param relativePath required Android Q
 * @param enablePending required Android Q
 */
@ExperimentalSugarApi
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

                MediaStoreType.Downloads -> MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }
        } else {
            when (type) {
                MediaStoreType.Audio -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

                MediaStoreType.Images -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                MediaStoreType.Video -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI

                else -> TODO("Not supported on old Android devices")
            }
        }

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!relativePath.isNullOrEmpty()) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            }
            if (enablePending) {
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
    }
    val uri = context.contentResolver.insert(collection, contentValues)!!
    return MediaFile(context, uri)
}