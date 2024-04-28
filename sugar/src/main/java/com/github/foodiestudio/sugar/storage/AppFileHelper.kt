package com.github.foodiestudio.sugar.storage

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import android.system.Os
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.documentfile.provider.DocumentFile
import com.github.foodiestudio.sugar.ExperimentalSugarApi
import com.github.foodiestudio.sugar.storage.filesystem.FileSystemCompat
import okio.FileSystem
import java.io.File
import java.io.FileOutputStream

/**
 * 针对仅应用持久化的文件，涉及 File 和 Cache。应用卸载后，这些数据也会同步被删除。
 *
 * 早期 Android 手机都支持存储卡扩展的，所以也会将应用存储分为两个，一个是空间比较小的内部存储，一个是空间比较大的外部存储。
 * 即便现在很多手机不支持SD卡扩展了，还是沿用了这个设计，只不过没有了实体的SD卡，改为 emulated 的外部存储。
 *
 * - 内部：/data/data/<package_name>/files
 * - 外部：/storage/emulated/0/Android/data/<package_name>/files
 *
 * 这里可以使用完整的 File API，不涉及 Uri 的使用，也没有任何权限的请求。
 */
@ExperimentalSugarApi
class AppFileHelper(private val applicationContext: Context) {

    init {
        check(applicationContext is Application) {
            "need valid applicationContext"
        }
    }

    private val internalRoot: File = applicationContext.filesDir
    private val internalCacheRoot: File = applicationContext.cacheDir

    // 不考虑多个外部存储的情况，目前支持SD卡的手机越来越少了
    private val externalRoot: File? = applicationContext.getExternalFilesDir(null)
    private val externalCacheRoot: File? = applicationContext.externalCacheDir

    private val storageManager: StorageManager =
        applicationContext.getSystemService(StorageManager::class.java)!!

    // 基于 okio 风格的 file path 操作文件
    val fileSystem: FileSystem = FileSystemCompat(applicationContext)

    fun requireFilesDir(sensitive: Boolean): File = if (sensitive) {
        internalRoot
    } else {
        externalRoot!!
    }

    fun requireCacheDir(sensitive: Boolean): File = if (sensitive) {
        internalCacheRoot
    } else {
        externalCacheRoot!!
    }

    /**
     * 在 [internalRoot] 创建一个临时文件, 文件名为 "[prefix]随机字符串.temp"
     */
    fun createTempFile(prefix: String = "Cache"): File {
        return File.createTempFile(prefix, null)
    }

    /**
     * 将 [requireCacheDir] 目录下 [cacheDir] 文件夹在被系统发起的缓存清理时，仅清除文件夹里的内容，而不删除这个文件夹。
     *
     * 这个主要为了区分某些情况下，这个缓存是否被创建过。
     *
     * 注意：如果是用户发起的缓存清空，这个文件夹还是会被删除。
     */
    fun markCacheDirAsTombstone(cacheDir: File) {
        storageManager.setCacheBehaviorTombstone(cacheDir, true)
    }

    /**
     * 检查当前是否还有足够的空间去创建 [cacheFileInBytes] 大小的缓存文件
     *
     * 这里的检查是考虑系统主动清理其他应用的缓存文件的前提下。
     *
     * 注意：如果事先不知道这个期望的文件大小，可以尝试立即写入文件，然后在出现 IOException 时将其捕获。
     *
     * @param sensitive 对应获取 [internalCacheRoot] 还是 [externalCacheRoot]
     *
     * 更多详情见，[官方文档](https://developer.android.com/reference/android/os/storage/StorageManager#getAllocatableBytes(java.util.UUID))
     */
    fun isCacheSpaceEnoughToCreate(cacheFileInBytes: Long, sensitive: Boolean): Boolean {
        val appSpecificUuid = storageManager.getUuidForPath(requireCacheDir(sensitive))
        return cacheFileInBytes <= storageManager.getAllocatableBytes(appSpecificUuid)
    }

    /**
     * 请求系统清除设备上的缓存文件（包括其他应用创建的）
     *
     * 请求前，需要先检查 [isCacheSpaceEnoughToCreate]，否则还是会失败。
     *
     * 注意，避免太频繁的调用（调用间隔应该大于60秒）
     *
     * 具体介绍见，[官方文档](https://developer.android.com/reference/android/os/storage/StorageManager#allocateBytes(java.io.FileDescriptor,%20long))
     */
    fun requestClearCacheForCreateFile(file: File, bytes: Long): Result<Unit> {
        return runCatching {
            storageManager.allocateBytes(file.outputStream().fd, bytes)
        }
    }

    /**
     * 获取应用缓存的额度（而非当前可用空间），这个值是固定的，不受当前应用的缓存文件夹里的文件大小影响。
     *
     * （以Android13的256G存储的手机为例，获取的额度为64MB）额度的机制是，当磁盘空间充足的情况下，是可以创建大于这个额度的文件，
     * 当磁盘空间不足的情况下，当前应用尝试在缓存文件夹下创建文件时，系统会优先把一些实际缓存使用大小已经超出额度的先清理掉。
     *
     * 也就是说，如果当前应用的缓存占用控制在这个额度以下的话，缓存文件将是系统上需要时最后清除的文件。
     *
     * 详情见，[官方文档](https://developer.android.com/about/versions/oreo/android-8.0?hl=zh-cn#cache)
     *
     * @param sensitive 对应获取 [internalCacheRoot] 还是 [externalCacheRoot]
     * @return 剩余 Cache 大小，单位为字节, 如需转为 MB，可以将除以 1024*1024
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @WorkerThread
    fun getCacheQuotaBytes(sensitive: Boolean): Result<Long> {
        val appSpecificUuid = storageManager.getUuidForPath(requireCacheDir(sensitive))
        return storageManager.runCatching {
            getCacheQuotaBytes(appSpecificUuid)
        }
    }

    /**
     * 往 [internalRoot] 里写入 [fileName] 文件，如果本身不存在这个文件的话，顺带会创建。
     *
     * @param fileName 文件名，例如，Foo.text，不能是 /Foo/Bar.text 这类包含路径的文件名
     */
    @WorkerThread
    @Deprecated("没什么实际用处的API", level = DeprecationLevel.HIDDEN)
    fun writeSensitiveFile(
        fileName: String,
        action: (FileOutputStream) -> Unit
    ) {
        applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            action(it)
        }
    }

    /**
     * [writeSensitiveFile] 的读取版本，如果本身不存在这个文件的话，顺带会创建。
     */
    @WorkerThread
    @Deprecated("没什么实际用处的API", level = DeprecationLevel.HIDDEN)
    fun readSensitiveFile(fileName: String): String {
        return applicationContext.openFileInput(fileName).bufferedReader().readLines()
            .joinToString("\n")
    }

    /**
     * 参考自 https://github.com/ya0211/MRepo/blob/a51c8b9fe21798902c0709d1d1bd719531d6a02f/app/src/main/kotlin/com/sanmer/mrepo/app/utils/MediaStoreUtils.kt#L59
     *
     * @param documentUri content://xxx/tree/abc/document/123 or content://xxx/document/123
     * @return 文件的绝对路径： /mnt/user/0/emulated/0/Download/ 等形式的路径
     */
    @Deprecated("这不是一个正经的用法，避免滥用，应该尽可能遵循 Scoped Storage。", level = DeprecationLevel.WARNING)
    @ExperimentalSugarApi
    fun getAbsoluteFilePath(documentUri: Uri): Result<String> = runCatching {
        // 文件夹无法直接使用，需要转为对应的 uri。这里会先尝试按 TreeUri 解析，如果解析失败，则尝试按 DocumentUri 解析
        val targetUri = runCatching {
            // 这一步的目的
            // documentUri: content://com.android.providers.downloads.documents/tree/msd%3A1000060615
            // 转化为
            // content://com.android.providers.downloads.documents/tree/msd%3A1000060615/document/msd%3A1000060615
            DocumentFile.fromTreeUri(applicationContext, documentUri)?.uri
        }.getOrDefault(documentUri)

        Log.v("AppFileHelper", "documentUri: $documentUri \n targetUri: $targetUri")

        applicationContext.contentResolver.openFileDescriptor(targetUri!!, "r")?.use {
            Os.readlink("/proc/self/fd/${it.fd}")
        }!!
    }

    /**
     * 在设备重启后保留对文件的访问权限并提供更出色的用户体验，您的应用可以“获取”系统提供的永久性 document URI 访问权限
     * 注意，仅限 OPEN_DOCUMENT 等 SAF 相关的 Intent，不包括 GET_CONTENT 这种
     */
    fun takePersistableUriPermission(documentUri: Uri) {
        val contentResolver = applicationContext.contentResolver

        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(documentUri, takeFlags)
    }
}
