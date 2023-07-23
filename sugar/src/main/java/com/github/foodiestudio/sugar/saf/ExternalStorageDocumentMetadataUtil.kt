package com.github.foodiestudio.sugar.saf

import android.content.ContentResolver
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract

internal const val ExternalStorageDocumentProvider_AUTHORITY =
    "com.android.externalstorage.documents"

internal fun queryExternalStorageDocumentFilePath(
    contentResolver: ContentResolver,
    uri: Uri
): String {
    val docId = DocumentsContract.getDocumentId(uri)
    val split = docId.split(":").toTypedArray()
    val type = split[0]
    // This is for checking Main Memory
    return if ("primary".equals(type, ignoreCase = true)) {
        if (split.size > 1) {
            Environment.getExternalStorageDirectory().toString() + "/" + split[1]
        } else {
            Environment.getExternalStorageDirectory().toString() + "/"
        }
        // This is for checking SD Card
    } else {
        "storage" + "/" + docId.replace(":", "/")
    }
}
