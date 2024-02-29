package com.github.foodiestudio.sugar.storage.filesystem.media

import android.content.Context
import android.provider.MediaStore
import com.github.foodiestudio.sugar.storage.filesystem.SharedFileSystem
import com.google.modernstorage.storage.MetadataExtras
import okio.FileMetadata
import okio.Path
import java.io.IOException

/**
 * 处理 Media Uri，与之对应的是 [com.github.foodiestudio.sugar.storage.filesystem.DocumentFileSystem]
 */
internal class MediaFileSystem(context: Context) : SharedFileSystem(context) {

    override fun atomicMove(source: Path, target: Path) {
        TODO("Not yet implemented")
    }

    override fun createDirectory(dir: Path, mustCreate: Boolean) {
        TODO("Not yet implemented")
    }

    override fun delete(path: Path, mustExist: Boolean) {
        if (!mustExist) {
            throw IOException("Deleting on an nonexistent path isn't supported ($path)")
        }
        val result = contentResolver.delete(
            path.toUri(),
            null,
            null
        )
        if (result == 0) {
            throw IOException("failed to delete $path")
        }
    }

    override fun list(dir: Path): List<Path> {
        TODO("Not yet implemented")
    }

    override fun metadataOrNull(path: Path): FileMetadata? {
        val uri = path.toUri()
        if (uri.pathSegments.firstOrNull().isNullOrBlank()) {
            return null
        }

        val isPhotoPickerUri = uri.pathSegments.firstOrNull() == "picker"

        val projection = if (isPhotoPickerUri) {
            arrayOf(
                MediaStore.MediaColumns.DATE_TAKEN,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATA,
            )
        } else {
            arrayOf(
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATA,
            )
        }

        val cursor = contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        ) ?: return null

        cursor.use {
            if (!it.moveToNext()) {
                return null
            }

            val createdTime: Long
            var lastModifiedTime: Long? = null

            if (isPhotoPickerUri) {
                createdTime = it.getLong(it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN))
            } else {
                createdTime = it.getLong(it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED))
                lastModifiedTime = it.getLong(it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED))
            }

            val displayName = it.getString(it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
            val mimeType = it.getString(it.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
            val size = it.getLong(it.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE))
            val filePath = it.getString(it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))

            return FileMetadata(
                isRegularFile = true,
                isDirectory = false,
                symlinkTarget = null,
                size = size,
                createdAtMillis = createdTime,
                lastModifiedAtMillis = lastModifiedTime,
                lastAccessedAtMillis = null,
                extras = mapOf(
                    Path::class to path,
                    MetadataExtras.DisplayName::class to MetadataExtras.DisplayName(displayName),
                    MetadataExtras.MimeType::class to MetadataExtras.MimeType(mimeType),
                    MetadataExtras.FilePath::class to MetadataExtras.FilePath(filePath),
                )
            )
        }
    }
}