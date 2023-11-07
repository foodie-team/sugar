package com.github.foodiestudio.sugar.saf

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.github.foodiestudio.sugar.ExperimentalSugarApi
import com.google.modernstorage.storage.AndroidFileSystem
import java.io.File

/**
 * 特指 **共享的** (images, audio files, videos) 这三类文件，这三类属于强用户隐私的文件，Android 版本越高，受限越多，
 * 也就是需要更多的用户授权
 *
 * READ_EXTERNAL_STORAGE when accessing other apps' files on Android 11 (API level 30) or higher
 *
 * READ_EXTERNAL_STORAGE or WRITE_EXTERNAL_STORAGE when accessing other apps' files on Android 10 (API level 29)
 *
 * Permissions are required for all files on Android 9 (API level 28) or lower
 *
 * 使用前需确保 [SAFHelper] 初始化过
 */
@ExperimentalSugarApi
object MediaStoreHelper {

    internal const val MediaDocumentProvider_AUTHORITY = "com.android.providers.media.documents"

    @SuppressLint("StaticFieldLeak")
    private val androidFileSystem: AndroidFileSystem = SAFHelper.fileSystem as AndroidFileSystem

    fun createMediaStoreUri(
        filename: String,
        collection: Uri = MediaStore.Files.getContentUri("external"),
        directory: String?
    ): Uri? = androidFileSystem.createMediaStoreUri(filename, collection, directory)

    suspend fun scanUri(uri: Uri, mimeType: String): Uri? = androidFileSystem.scanUri(uri, mimeType)

    suspend fun scanFile(file: File, mimeType: String): Uri? =
        androidFileSystem.scanFile(file, mimeType)

    internal fun getDisplayName(contentResolver: ContentResolver, uri: Uri?): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME
        )
        try {
            if (uri == null) return null
            cursor = contentResolver.query(
                uri, projection, null, null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }
}