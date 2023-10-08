package com.github.foodiestudio.sugar.saf

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.system.Os
import androidx.documentfile.provider.DocumentFile
import com.github.foodiestudio.sugar.ExperimentalSugarApi
import com.google.modernstorage.permissions.StoragePermissions
import com.google.modernstorage.storage.AndroidFileSystem
import com.google.modernstorage.storage.MetadataExtras
import com.google.modernstorage.storage.toOkioPath
import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.ForwardingFileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import java.io.File

/**
 * SAF 相关, 使用前记得先初始化
 */
@SuppressLint("StaticFieldLeak")
@ExperimentalSugarApi
object SAFHelper {
    // okio 定义的 FileSystem 接口
    lateinit var fileSystem: FileSystem

    internal lateinit var appContext: Context

    internal lateinit var storagePermissions: StoragePermissions

    /**
     * 初始化
     */
    fun init(context: Context) {
        appContext = context.applicationContext
        storagePermissions = StoragePermissions(appContext)
        // workaround: AndroidFileSystem 没有实现的几个方法使用 SYSTEM 实现
        fileSystem = object : ForwardingFileSystem(AndroidFileSystem(appContext)) {
            override fun createDirectory(dir: Path, mustCreate: Boolean) =
                SYSTEM.createDirectory(dir, mustCreate)

            override fun atomicMove(source: Path, target: Path) = SYSTEM.atomicMove(source, target)

            override fun openReadOnly(file: Path): FileHandle = SYSTEM.openReadOnly(file)

            override fun openReadWrite(
                file: Path, mustCreate: Boolean, mustExist: Boolean
            ): FileHandle = SYSTEM.openReadWrite(file, mustCreate, mustExist)
        }
    }
}

@ExperimentalSugarApi
fun SAFHelper.copy(source: Uri, dest: File) =
    fileSystem.copy(source.toOkioPath(), dest.toOkioPath())

@ExperimentalSugarApi
fun SAFHelper.metadataOrNull(uri: Uri): FileMetadata? = fileSystem.metadataOrNull(uri.toOkioPath())

/**
 * 查询 [uri] 对应的 MimeType，查询不到的情况为返回 null
 * 不同 Android 版本所能识别的类型是不同的，例如: 字体文件 `font/ttf` 在低版本会被当作 `application/octet-stream`
 */
@ExperimentalSugarApi
fun SAFHelper.mimeTypeOrNull(uri: Uri): String? =
    fileSystem.metadataOrNull(uri.toOkioPath())?.extra(
        MetadataExtras.MimeType::class
    )?.value

/**
 * 查询 [uri] 对应的文件名，查询不到的情况为返回 null
 * 可能会返回的是一个 id，而非真实的文件名，另一种
 */
@ExperimentalSugarApi
fun SAFHelper.displayNameOrNull(uri: Uri): String? =
    fileSystem.metadataOrNull(uri.toOkioPath())?.extra(MetadataExtras.DisplayName::class)?.value

/**
 * 查询 [uri] 对应的文件绝对路经，查询不到的情况为返回 null
 *
 * 不是很推荐获取文件路经，因为获取到 uri 就能获得 inputStream/outputStream，就能进行，一定需要 path 的情况，
 * 可以考虑先把文件 copy 到 cache 路径，然后完成下一步的业务处理
 *
 * 还有一些第三方应用提供的 uri，比如：坚果云、Google Driver 等。
 *
 * Scoped Storage 的机制也是希望保护用户的隐私，避免只是访问 File Path，取而代之的是 DocumentFile
 */
@ExperimentalSugarApi
@Deprecated("scoped storage 基本上是拿不到路径的，请使用 DocumentFile 来代替")
fun SAFHelper.absoluteFilePathOrNull(uri: Uri): String? =
    fileSystem.metadataOrNull(uri.toOkioPath())?.extra(
        MetadataExtras.FilePath::class
    )?.value

/**
 * 参考自 https://github.com/ya0211/MRepo/blob/a51c8b9fe21798902c0709d1d1bd719531d6a02f/app/src/main/kotlin/com/sanmer/mrepo/app/utils/MediaStoreUtils.kt#L59
 *
 * @return 查询不到的话，会返回自身
 */
// TODO: 没有进行过比较多的测试, 仅在极少数情况下使用，避免滥用，应该尽可能遵循 Scoped Storage
@ExperimentalSugarApi
fun SAFHelper.absoluteFilePath(uri: Uri): String = absoluteFilePathOrNull(uri) ?: run {
    val targetUri = runCatching {
        DocumentFile.fromTreeUri(appContext, uri)?.uri ?: return uri.toString()
    }.getOrDefault(uri)

    appContext.contentResolver.openFileDescriptor(targetUri, "r")?.use {
        Os.readlink("/proc/self/fd/${it.fd}")
    } ?: uri.toString()
}
