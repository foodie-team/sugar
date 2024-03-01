package com.github.foodiestudio.sugar.storage.filesystem.media

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.WorkerThread
import kotlin.time.Duration

internal class SingleMediaStoreFilterCondition<T>(
    val fieldName: String,
    val fieldValue: T,
    val operator: String
) {
    val selection: String get() = "$fieldName $operator ?"
    val selectionArgs: Array<String> get() = arrayOf(fieldValue.toString())
}

internal infix fun String.greaterEq(duration: Duration) =
    SingleMediaStoreFilterCondition(
        fieldName = this,
        fieldValue = duration.inWholeMilliseconds,
        operator = ">="
    )

internal class MediaStoreColumnParser(private val cursor: Cursor) {
    private val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
    private val nameColumn =
        cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
    private val relativePathColumn =
        cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
    private val durationColumn =
        cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION)
    private val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)

    // 视频的绝对路径
    // 另一方面，如需创建或更新媒体文件，请勿使用 DATA 列的值。请改用 DISPLAY_NAME 和 RELATIVE_PATH 列的值。
    private val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)

    val id: Long get() = cursor.getLong(idColumn)
    val name: String get() = cursor.getString(nameColumn)
    val duration: Int get() = cursor.getInt(durationColumn)
    val size: Int get() = cursor.getInt(sizeColumn)

    val contentUri: Uri = ContentUris.withAppendedId(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        id
    )

    val relativePath: String get() = cursor.getString(relativePathColumn)

    // 视频的绝对路径,不要假设文件始终可用。
    val filePath: String get() = cursor.getString(dataColumn)
}

internal class MediaStoreUpdateBuilder(private val resolver: ContentResolver) {
    /**
     * @param mediaUri 包含 id 的 media Store uri
     */
    fun update(mediaUri: Uri, builder: ContentValues.() -> Unit): Int {
        val id: Long =
            ContentUris.parseId(mediaUri).takeIf { it != -1L }
                ?: throw IllegalArgumentException("can't parse id from $mediaUri")
        val newSongDetails = ContentValues().apply {
            builder(this)
        }
        val selection = "${MediaStore.MediaColumns._ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        return resolver.update(
            mediaUri,
            newSongDetails,
            selection,
            selectionArgs
        )
    }
}