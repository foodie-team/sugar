package com.github.foodiestudio.sugar.saf

import android.net.Uri
import android.provider.MediaStore
import java.io.File

/**
 * 使用前需确保 [SAFHelper] 初始化过
 */
object MediaStoreHelper {

    fun createMediaStoreUri(
        filename: String,
        collection: Uri = MediaStore.Files.getContentUri("external"),
        directory: String?
    ): Uri? = SAFHelper.fileSystem.createMediaStoreUri(filename, collection, directory)

    suspend fun scanUri(uri: Uri, mimeType: String): Uri? =
        SAFHelper.fileSystem.scanUri(uri, mimeType)

    suspend fun scanFile(file: File, mimeType: String): Uri? =
        SAFHelper.fileSystem.scanFile(file, mimeType)
}