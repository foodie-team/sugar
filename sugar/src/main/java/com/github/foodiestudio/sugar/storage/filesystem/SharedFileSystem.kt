package com.github.foodiestudio.sugar.storage.filesystem

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import okio.FileHandle
import okio.FileSystem
import okio.Path
import okio.Sink
import okio.Source
import okio.sink
import okio.source
import java.io.IOException

/**
 * 基于 uri 的文件系统
 */
internal abstract class SharedFileSystem(context: Context) : FileSystem() {
    protected val contentResolver: ContentResolver = context.contentResolver

    protected fun Path.toUri(): Uri {
        val str = this.toString()

        if (str.startsWith("content:/")) {
            return Uri.parse(str.replace("content:/", "content://"))
        }

        return Uri.parse(str)
    }

    override fun appendingSink(file: Path, mustExist: Boolean): Sink {
        val uri = file.toUri()
        if (!mustExist) {
            throw IOException("Appending on an nonexistent path isn't supported ($file)")
        }
        val outputStream = contentResolver.openOutputStream(uri, "a")

        if (outputStream == null) {
            throw IOException("Couldn't open an OutputStream ($file)")
        } else {
            return outputStream.sink()
        }
    }

    override fun createSymlink(source: Path, target: Path) {
        TODO("Not yet implemented")
    }

    override fun canonicalize(path: Path): Path =
        throw UnsupportedOperationException("Paths can't be canonical in AndroidFileSystem")

    override fun openReadOnly(file: Path): FileHandle {
        TODO("Not yet implemented")
    }

    // maybe can implement in this way: https://stackoverflow.com/questions/28897329/documentfile-randomaccessfile
    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle {
        TODO("Not yet implemented")
    }

    override fun sink(file: Path, mustCreate: Boolean): Sink {
        if (mustCreate) {
            throw IOException("Path creation isn't supported ($file)")
        }

        val uri = file.toUri()
        val outputStream = contentResolver.openOutputStream(uri)

        if (outputStream == null) {
            throw IOException("Couldn't open an OutputStream ($file)")
        } else {
            return outputStream.sink()
        }
    }

    override fun source(file: Path): Source {
        val uri = file.toUri()
        val inputStream = contentResolver.openInputStream(uri)

        if (inputStream == null) {
            throw IOException("Couldn't open an InputStream ($file)")
        } else {
            return inputStream.source()
        }
    }

    override fun listOrNull(dir: Path): List<Path>? = runCatching { list(dir) }.getOrNull()
}