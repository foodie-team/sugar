package com.github.foodiestudio.sugar.storage.filesystem

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.google.modernstorage.storage.MetadataExtras
import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Sink
import okio.Source
import okio.sink
import okio.source
import java.io.IOException

/**
 * 针对 SAF 方式获得 Document Uri，以这个 uri 作为 Path 来进行操作
 *
 * 参考：https://developer.android.com/training/data-storage/shared/documents-files
 */
internal class DocumentFileSystem(private val context: Context) : SharedFileSystem(context) {
    /**
     * 如果都是 Document uri 的话，倒是可以。其他情况不行。
     * workaround 的方式是，先通过 copy，然后再删除源文件
     *
     * @param source 源文件，非文件夹类型
     * @param target 目标文件夹
     */
    override fun atomicMove(source: Path, target: Path) {
        require(
            DocumentFile.isDocumentUri(context, source.toUri()) && DocumentFile.isDocumentUri(
                context,
                target.toUri()
            )
        ) {
            "source and target must be Document uri"
        }
        val sourceDocument = DocumentFile.fromSingleUri(context, source.toUri())!!
        val targetDocumentFolder = DocumentFile.fromTreeUri(context, target.toUri())!!

        DocumentsContract.moveDocument(
            contentResolver,
            sourceDocument.uri,
            sourceDocument.parentFile!!.uri,
            targetDocumentFolder.uri
        )
    }

    override fun createDirectory(dir: Path, mustCreate: Boolean) {
        TODO("Please use TreeDocumentFile.createDirectory(dirName)")
    }

    override fun delete(path: Path, mustExist: Boolean) {
        if (!mustExist) {
            throw IOException("Deleting on an nonexistent path isn't supported ($path)")
        }

        val uri = path.toUri()
        // the document's Document.COLUMN_FLAGS contains SUPPORTS_DELETE, you can delete the document.
        val deleted = DocumentsContract.deleteDocument(contentResolver, uri)

        if (!deleted) {
            throw IOException("failed to delete $path")
        }
    }

    override fun list(dir: Path): List<Path> =
        DocumentFile.fromTreeUri(context, dir.toUri())?.listFiles()?.map {
            it.uri.toOkioPath()
        }!!

    private fun fetchMetadataFromDocumentProvider(uri: Uri): FileMetadata? {
        val array = arrayOf(
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE
        )
        val cursor = contentResolver.query(
            uri,
            array,
            null,
            null,
            null
        ) ?: return null

        cursor.use {
            if (!it.moveToNext()) {
                return null
            }

            // DocumentsContract.Document.COLUMN_LAST_MODIFIED
            val lastModifiedTime = it.getLong(array.indexOf(DocumentsContract.Document.COLUMN_LAST_MODIFIED))
            // DocumentsContract.Document.COLUMN_DISPLAY_NAME
            val displayName = it.getString(array.indexOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
            // DocumentsContract.Document.COLUMN_MIME_TYPE
            val mimeType = it.getString(array.indexOf(DocumentsContract.Document.COLUMN_MIME_TYPE))
            // DocumentsContract.Document.COLUMN_SIZE
            val size = it.getLong(array.indexOf(DocumentsContract.Document.COLUMN_SIZE))

            val isFolder = mimeType == DocumentsContract.Document.MIME_TYPE_DIR ||
                    mimeType == DocumentsContract.Root.MIME_TYPE_ITEM

            return FileMetadata(
                isRegularFile = !isFolder,
                isDirectory = isFolder,
                symlinkTarget = null,
                size = size,
                createdAtMillis = null,
                lastModifiedAtMillis = lastModifiedTime,
                lastAccessedAtMillis = null,
                extras = mapOf(
                    Uri::class to uri,
                    MetadataExtras.DisplayName::class to MetadataExtras.DisplayName(displayName),
                    MetadataExtras.MimeType::class to MetadataExtras.MimeType(mimeType),
                )
            )
        }
    }

    override fun metadataOrNull(path: Path): FileMetadata? {
        val uri = path.toUri()
        return if (DocumentFile.isDocumentUri(context, uri)) {
            fetchMetadataFromDocumentProvider(uri)
        } else {
            TODO("unsupported path: $path, please use MediaFile")
        }
    }
}