package com.github.foodiestudio.sugar.saf

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import java.io.File

// 文件选择器中的「DownLoad」入口进来的
internal const val DownloadDocumentProvider_AUTHORITY = "com.android.providers.downloads.documents"

internal fun queryDownloadDocumentFilePath(contentResolver: ContentResolver, uri: Uri): String? {
    val fileName = MediaStoreHelper.getDisplayName(contentResolver, uri)
    if (fileName != null) {
        return Environment.getExternalStorageDirectory()
            .toString() + "/Download/" + fileName
    }
    var id = DocumentsContract.getDocumentId(uri)
    if (id.startsWith("raw:")) {
        id = id.replaceFirst("raw:".toRegex(), "")
        val file = File(id)
        if (file.exists()) return id
    }
    val contentUri = ContentUris.withAppendedId(
        Uri.parse("content://downloads/public_downloads"),
        java.lang.Long.valueOf(id)
    )
    return getDataColumn(contentResolver, contentUri, null, null)
}

private fun getDataColumn(
    contentResolver: ContentResolver, uri: Uri?, selection: String?,
    selectionArgs: Array<String>?
): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(
        column
    )
    try {
        if (uri == null) return null
        cursor = contentResolver.query(
            uri, projection, selection, selectionArgs,
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } finally {
        cursor?.close()
    }
    return null
}
