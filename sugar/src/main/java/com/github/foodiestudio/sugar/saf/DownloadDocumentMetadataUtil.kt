package com.github.foodiestudio.sugar.saf

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import java.io.File

// https://stackoverflow.com/questions/17546101/get-real-path-for-uri-android/61995806#61995806

internal const val DownloadDocumentProvider_AUTHORITY = "com.android.providers.downloads.documents"

internal fun queryDownloadDocumentFilePath(contentResolver: ContentResolver, uri: Uri): String? {
    // TODO: 查询的是 MediaStore 的文件名，非 Media 是否能起作用待测试
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

internal fun getDataColumn(
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
