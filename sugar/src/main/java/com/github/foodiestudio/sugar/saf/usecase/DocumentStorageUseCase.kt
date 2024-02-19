package com.github.foodiestudio.sugar.saf.usecase

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.documentfile.provider.DocumentFile
import com.github.foodiestudio.sugar.ExperimentalSugarApi
import com.github.foodiestudio.sugar.saf.SAFHelper
import com.github.foodiestudio.sugar.saf.hasWriteAccessByAllApps
import com.google.modernstorage.permissions.StoragePermissions
import okio.BufferedSink
import okio.Path

/**
 * File 访问有几个版本做了调整
 *
 * Android 10:
 * Android 13:
 * Android 14:
 * */
@ExperimentalSugarApi
interface DocumentStorageUseCase {
    // Open a document file : rememberLauncherForActivityResult

    // Write to files on secondary storage volumes
    fun <T> writeToReliableVolume(
        file: Path,
        mustCreate: Boolean,
        writerAction: BufferedSink.() -> T
    ): T

    // Migrate existing files from a legacy storage location: 纯经验之谈，没啥好写的

    // Share content with other apps：使用 FileProvider

    // Cache non-media files:

    // Export non-media files to a device
}

@ExperimentalSugarApi
class DocumentStorageUseCaseImpl : DocumentStorageUseCase {

    @SuppressLint("MissingPermission")
    override fun <T> writeToReliableVolume(
        file: Path,
        mustCreate: Boolean,
        writerAction: BufferedSink.() -> T
    ): T {
        check(SAFHelper.hasWriteAccessByAllApps(StoragePermissions.FileType.values().toList()))

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            DocumentStorageUseCaseApi29Impl().writeToReliableVolume(file, mustCreate, writerAction)
        } else {
            DocumentStorageUseCaseBaseImpl().writeToReliableVolume(file, mustCreate, writerAction)
        }
    }
}

@ExperimentalSugarApi
private open class DocumentStorageUseCaseBaseImpl : DocumentStorageUseCase {

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    override fun <T> writeToReliableVolume(
        file: Path,
        mustCreate: Boolean,
        writerAction: BufferedSink.() -> T
    ): T = SAFHelper.fileSystem.write(file, mustCreate, writerAction)

}

// Android 10
@ExperimentalSugarApi
@RequiresApi(Build.VERSION_CODES.Q)
private class DocumentStorageUseCaseApi29Impl : DocumentStorageUseCaseBaseImpl() {
}

//// Android 11
//@ExperimentalSugarApi
//@RequiresApi(Build.VERSION_CODES.R)
//private class DocumentStorageUseCaseApi30Impl : DocumentStorageUseCase {
//
//}



