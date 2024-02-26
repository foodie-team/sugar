package com.github.foodiestudio.sugar.storage.filesystem.media

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import okio.FileMetadata

/**
 * MediaStore 实际上也包括非 Media 的文件，只是这里都是存放在 MediaStore
 * 这个预设「文件夹」下
 */

internal enum class MediaStoreType {
    Images, Audio, Video, Downloads
}

/**
 * 暂仅讨论开启 Scoped Storage 的情况
 *
 * mediaUri 的格式应该是 "content://media/" 开头
 */
@RequiresApi(Build.VERSION_CODES.Q)
internal class MediaFileCompat(context: Context) {

    private val resolver = context.contentResolver

    fun createFile(store: MediaStoreType, fileName: String, relativePath: String? = null) {

    }

    fun delete() {
        // TODO:
    }

    fun moveTo(mediaUri: Uri, newRelativePath: String) {
        MediaStoreUpdateBuilder(resolver).update(mediaUri) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, newRelativePath)
        }
    }

    fun rename(mediaUri: Uri, displayName: String) {
        MediaStoreUpdateBuilder(resolver).update(mediaUri) {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        }
    }

    /**
     * 可以查询对应的 DisplayName、MineType 等
     */
    val metadata: FileMetadata?
        get() = TODO()

}