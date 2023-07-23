package com.github.foodiestudio.sugar.saf

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import com.github.foodiestudio.sugar.saf.MediaStoreHelper.MediaDocumentProvider_AUTHORITY
import com.google.modernstorage.permissions.StoragePermissions
import com.google.modernstorage.storage.AndroidFileSystem
import com.google.modernstorage.storage.MetadataExtras
import com.google.modernstorage.storage.toOkioPath
import com.google.modernstorage.storage.toUri
import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.Sink
import okio.Source
import java.io.File


/**
 * SAF 相关, 使用前记得先初始化
 */
@SuppressLint("StaticFieldLeak")
object SAFHelper : FileSystem() {
    internal lateinit var fileSystem: AndroidFileSystem

    internal lateinit var storagePermissions: StoragePermissions

    private lateinit var contentResolver: ContentResolver

    /**
     * 初始化
     */
    fun init(application: Application) {
        fileSystem = AndroidFileSystem(application)
        storagePermissions = StoragePermissions(application)
        contentResolver = application.contentResolver
    }

    override fun copy(source: Path, target: Path) {
        fileSystem.copy(source = source, target = target)
    }

    /**
     * get metadata, for example:
     * ```
     * displayName: extra(DisplayName::class).value
     * mimeType: extra(MimeType::class).value
     * ```
     */
    override fun metadataOrNull(path: Path): FileMetadata? {
        val uri = path.toUri()

        // FIXME: fileSystem 并未处理 content provider 情况时的 filePath
        // FIXME: 主要参考了 https://stackoverflow.com/questions/17546101/get-real-path-for-uri-android/61995806#61995806
        return when (uri.authority) {
            // Downloads 入口
            DownloadDocumentProvider_AUTHORITY -> fetchMetadataFromDocumentProvider(
                contentResolver, path, uri, ::queryDownloadDocumentFilePath
            )

            // 「手机名称」的入口
            ExternalStorageDocumentProvider_AUTHORITY -> fetchMetadataFromDocumentProvider(
                contentResolver, path, uri, ::queryExternalStorageDocumentFilePath
            )

            else -> fileSystem.metadataOrNull(path)
        }
    }

    override fun appendingSink(file: Path, mustExist: Boolean): Sink =
        fileSystem.appendingSink(file, mustExist)

    // TODO:
    override fun atomicMove(source: Path, target: Path) = fileSystem.atomicMove(source, target)

    override fun canonicalize(path: Path): Path = fileSystem.canonicalize(path)

    override fun createDirectory(dir: Path, mustCreate: Boolean) {
        if (!dir.toFile().mkdir()) {
            val alreadyExist = metadataOrNull(dir)?.isDirectory == true
            if (alreadyExist) {
                if (mustCreate) {
                    throw IOException("$dir already exist.")
                } else {
                    return
                }
            }
            throw IOException("failed to create directory: $dir")
        }
    }

    override fun createSymlink(source: Path, target: Path) =
        fileSystem.createSymlink(source, target)

    override fun delete(path: Path, mustExist: Boolean) = fileSystem.delete(path, mustExist)

    override fun list(dir: Path): List<Path> = fileSystem.list(dir)

    override fun listOrNull(dir: Path): List<Path>? = fileSystem.listOrNull(dir)

    // TODO:
    override fun openReadOnly(file: Path): FileHandle = fileSystem.openReadOnly(file)

    // TODO:
    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle =
        fileSystem.openReadWrite(file, mustCreate, mustExist)

    override fun sink(file: Path, mustCreate: Boolean): Sink = fileSystem.sink(file, mustCreate)

    override fun source(file: Path): Source = fileSystem.source(file)
}

fun SAFHelper.copy(source: Uri, dest: File) {
    copy(source.toOkioPath(), dest.toOkioPath())
}

fun SAFHelper.metadataOrNull(uri: Uri): FileMetadata? = metadataOrNull(uri.toOkioPath())

/**
 * 查询 [uri] 对应的 MimeType，查询不到的情况为返回 null
 * 不同 Android 版本所能识别的类型是不同的，例如: 字体文件 `font/ttf` 在低版本会被当作 `application/octet-stream`
 */
fun SAFHelper.mimeTypeOrNull(uri: Uri): String? = metadataOrNull(uri.toOkioPath())?.extra(
    MetadataExtras.MimeType::class
)?.value

/**
 * 查询 [uri] 对应的文件绝对路经，查询不到的情况为返回 null
 *
 * 不是很推荐获取文件路经，因为获取到 uri 就能获得 inputStream/outputStream，就能进行
 *
 * 已知问题：
 * [MediaDocumentProvider_AUTHORITY] 类型的 uri 无法获取 FilePath，虽然在高版本上 MediaStore 提供了转化的 API，但似乎也不是出于这个目的？
 * 还有一些第三方应用提供的 uri，比如：坚果云、Google Driver 等
 */
fun SAFHelper.absoluteFilePathOrNull(uri: Uri): String? = metadataOrNull(uri.toOkioPath())?.extra(
    MetadataExtras.FilePath::class
)?.value

/**
 * 查询 [uri] 对应的文件名，查询不到的情况为返回 null
 * 可能会返回的是一个 id，而非真实的文件名，另一种
 */
fun SAFHelper.displayNameOrNull(uri: Uri): String? =
    metadataOrNull(uri.toOkioPath())?.extra(MetadataExtras.DisplayName::class)?.value

internal fun fetchMetadataFromDocumentProvider(
    contentResolver: ContentResolver,
    path: Path,
    uri: Uri,
    filePathQuery: (ContentResolver, Uri) -> String?
): FileMetadata? {
    val cursor = contentResolver.query(
        uri,
        arrayOf(
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE
        ),
        null,
        null,
        null
    ) ?: return null

    cursor.use { cursor ->
        if (!cursor.moveToNext()) {
            return null
        }

        // DocumentsContract.Document.COLUMN_LAST_MODIFIED
        val lastModifiedTime = cursor.getLong(0)
        // DocumentsContract.Document.COLUMN_DISPLAY_NAME
        val displayName = cursor.getString(1)
        // DocumentsContract.Document.COLUMN_MIME_TYPE
        val mimeType = cursor.getString(2)
        // DocumentsContract.Document.COLUMN_SIZE
        val size = cursor.getLong(3)

        val filePath = filePathQuery(contentResolver, uri)

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
            extras = mutableMapOf(
                Path::class to path,
                Uri::class to uri,
                MetadataExtras.DisplayName::class to MetadataExtras.DisplayName(displayName),
                MetadataExtras.MimeType::class to MetadataExtras.MimeType(mimeType),
            ).also {
                if (filePath != null) {
                    it[MetadataExtras.FilePath::class] = MetadataExtras.FilePath(filePath)
                }
            }
        )
    }
}
