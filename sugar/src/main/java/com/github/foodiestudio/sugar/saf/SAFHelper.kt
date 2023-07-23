package com.github.foodiestudio.sugar.saf

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import com.google.modernstorage.permissions.StoragePermissions
import com.google.modernstorage.storage.AndroidFileSystem
import com.google.modernstorage.storage.MetadataExtras
import com.google.modernstorage.storage.toOkioPath
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

    /**
     * 初始化
     */
    fun init(application: Application) {
        fileSystem = AndroidFileSystem(application)
        storagePermissions = StoragePermissions(application)
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
    override fun metadataOrNull(path: Path): FileMetadata? = fileSystem.metadataOrNull(path)

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
 */
fun SAFHelper.absoluteFilePathOrNull(uri: Uri): String? = metadataOrNull(uri.toOkioPath())?.extra(
    MetadataExtras.FilePath::class
)?.value