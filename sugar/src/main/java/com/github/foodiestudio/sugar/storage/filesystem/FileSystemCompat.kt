package com.github.foodiestudio.sugar.storage.filesystem

import android.content.Context
import android.net.Uri
import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source

/**
 * [FileSystemCompat] 相关操作都是基于 okio 的 path
 */
fun Uri.toOkioPath(): Path = this.toString().toPath(false)

/**
 * 操作对应的 [FileSystem] 时，需要确保有对应的权限
 */
internal class FileSystemCompat(context: Context) : FileSystem() {

    // 针对处理 document uri (SAF 方式)
    private val documentFileSystem = DocumentFileSystem(context)

    // 应用的专属目录下的文件，基于 file 的 path
    private val appFileSystem = SYSTEM

    private fun isPhysicalFile(path: Path): Boolean {
        return path.toString().first() == '/'
    }

    override fun appendingSink(file: Path, mustExist: Boolean): Sink = if (isPhysicalFile(file)) {
        appFileSystem.appendingSink(file, mustExist)
    } else {
        documentFileSystem.appendingSink(file, mustExist)
    }

    /**
     * 仅支持同类型的路径，要不都是 file，都不都是 uri
     */
    override fun atomicMove(source: Path, target: Path) {
        when {
            isPhysicalFile(source) && isPhysicalFile(target) -> {
                appFileSystem.atomicMove(source, target)
            }
            !isPhysicalFile(source) && !isPhysicalFile(target) -> {
                documentFileSystem.atomicMove(source, target)
            }
            else -> TODO("Not yet implemented")
        }
    }

    override fun canonicalize(path: Path): Path = if (isPhysicalFile(path)) {
        appFileSystem.canonicalize(path)
    } else {
        documentFileSystem.canonicalize(path)
    }

    override fun createDirectory(dir: Path, mustCreate: Boolean) = if (isPhysicalFile(dir)) {
        appFileSystem.createDirectory(dir, mustCreate)
    } else {
        documentFileSystem.createDirectory(dir, mustCreate)
    }

    /**
     * 仅支持同类型的路径，要不都是 file，都不都是 uri
     */
    override fun createSymlink(source: Path, target: Path) {
        when {
            isPhysicalFile(source) && isPhysicalFile(target) -> {
                appFileSystem.createSymlink(source, target)
            }
            !isPhysicalFile(source) && !isPhysicalFile(target) -> {
                documentFileSystem.createSymlink(source, target)
            }
            else -> TODO("Not yet implemented")
        }
    }

    override fun delete(path: Path, mustExist: Boolean) = if (isPhysicalFile(path)) {
        appFileSystem.delete(path, mustExist)
    } else {
        documentFileSystem.delete(path, mustExist)
    }

    @Throws(IOException::class)
    override fun list(dir: Path): List<Path> = if (isPhysicalFile(dir)) {
        appFileSystem.list(dir)
    } else {
        documentFileSystem.list(dir)
    }

    override fun listOrNull(dir: Path): List<Path>? = if (isPhysicalFile(dir)) {
        appFileSystem.listOrNull(dir)
    } else {
        documentFileSystem.listOrNull(dir)
    }

    override fun metadataOrNull(path: Path): FileMetadata? = if (isPhysicalFile(path)) {
        appFileSystem.metadataOrNull(path)
    } else {
        documentFileSystem.metadataOrNull(path)
    }

    override fun openReadOnly(file: Path): FileHandle = if (isPhysicalFile(file)) {
        appFileSystem.openReadOnly(file)
    } else {
        documentFileSystem.openReadOnly(file)
    }

    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle =
        if (isPhysicalFile(file)) {
            appFileSystem.openReadWrite(file, mustCreate, mustExist)
        } else {
            documentFileSystem.openReadWrite(file, mustCreate, mustExist)
        }

    override fun sink(file: Path, mustCreate: Boolean): Sink = if (isPhysicalFile(file)) {
        appFileSystem.sink(file, mustCreate)
    } else {
        documentFileSystem.sink(file, mustCreate)
    }

    override fun source(file: Path): Source = if (isPhysicalFile(file)) {
        appFileSystem.source(file)
    } else {
        documentFileSystem.source(file)
    }
}