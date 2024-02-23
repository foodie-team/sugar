package com.github.foodiestudio.sugar.storage.filesystem

import android.content.Context
import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Sink
import okio.Source

/**
 * 可以参考 DocumentFile 和 [DocumentFileSystem]
 */
// TODO(Jiangc):
class MediaFileSystem(private val context: Context) : FileSystem() {

    override fun appendingSink(file: Path, mustExist: Boolean): Sink {
        TODO("Not yet implemented")
    }

    override fun atomicMove(source: Path, target: Path) {
        TODO("Not yet implemented")
    }

    override fun canonicalize(path: Path): Path {
        TODO("Not yet implemented")
    }

    override fun createDirectory(dir: Path, mustCreate: Boolean) {
        TODO("Not yet implemented")
    }

    override fun createSymlink(source: Path, target: Path) {
        TODO("Not yet implemented")
    }

    override fun delete(path: Path, mustExist: Boolean) {
        TODO("Not yet implemented")
    }

    override fun list(dir: Path): List<Path> {
        TODO("Not yet implemented")
    }

    override fun listOrNull(dir: Path): List<Path>? {
        TODO("Not yet implemented")
    }

    override fun metadataOrNull(path: Path): FileMetadata? {
        TODO("Not yet implemented")
    }

    override fun openReadOnly(file: Path): FileHandle {
        TODO("Not yet implemented")
    }

    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle {
        TODO("Not yet implemented")
    }

    override fun sink(file: Path, mustCreate: Boolean): Sink {
        TODO("Not yet implemented")
    }

    override fun source(file: Path): Source {
        TODO("Not yet implemented")
    }
}