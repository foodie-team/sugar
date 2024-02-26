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

class SingleMediaStoreFilterCondition<T>(
    val fieldName: String,
    val fieldValue: T,
    val operator: String
) {
    val selection: String get() = "$fieldName $operator ?"
    val selectionArgs: Array<String> get() = arrayOf(fieldValue.toString())
}

infix fun String.greaterEq(duration: Duration) =
    SingleMediaStoreFilterCondition(
        fieldName = this,
        fieldValue = duration.inWholeMilliseconds,
        operator = ">="
    )

class MediaStoreColumnParser(private val cursor: Cursor) {
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


class MediaStoreInsertBuilder(private val resolver: ContentResolver) {
    private val audioCollection =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

    fun new(builder: ContentValues.() -> Unit): Uri? {
        val newSongDetails = ContentValues().apply {
            builder(this)
        }
        return resolver.insert(audioCollection, newSongDetails)
    }
}

class MediaStoreUpdateBuilder(private val resolver: ContentResolver) {
    private val audioCollection =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

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

class MediaStoreQueryBuilder(private val resolver: ContentResolver) {

    private lateinit var filterCondition: SingleMediaStoreFilterCondition<Any>

    fun <T : Any> find(condition: () -> SingleMediaStoreFilterCondition<T>) = apply {
        // TODO: 正确处理范型
        filterCondition = condition() as SingleMediaStoreFilterCondition<Any>
    }

    @WorkerThread
    fun <T> collect(convert: MediaStoreColumnParser.() -> T): List<T> {
        // TODO: 暂时写死projection
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        )

        // TODO: 暂时写死projection
        val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

        val selection = filterCondition.selection
        val selectionArgs = filterCondition.selectionArgs

        val result = mutableListOf<T>()

        resolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val parser by lazy(LazyThreadSafetyMode.NONE) { MediaStoreColumnParser(cursor) }
            while (cursor.moveToNext()) {
                result.add(convert(parser))
            }
        }
        return result
    }
}