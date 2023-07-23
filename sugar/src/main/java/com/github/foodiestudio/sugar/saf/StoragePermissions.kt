package com.github.foodiestudio.sugar.saf

import com.google.modernstorage.permissions.StoragePermissions

/**
 * [请求权限查看](https://google.github.io/modernstorage/permissions/#request-storage-permissions)
 */
fun SAFHelper.hasReadAccessBySelf(types: List<StoragePermissions.FileType>): Boolean =
    storagePermissions.hasAccess(
        StoragePermissions.Action.READ,
        types,
        StoragePermissions.CreatedBy.Self
    )

/**
 * 有写权限自然有读权限
 * [请求权限查看](https://google.github.io/modernstorage/permissions/#request-storage-permissions)
 */
fun SAFHelper.hasWriteAccessBySelf(types: List<StoragePermissions.FileType>): Boolean =
    storagePermissions.hasAccess(
        StoragePermissions.Action.READ_AND_WRITE,
        types,
        StoragePermissions.CreatedBy.Self
    )

/**
 * [请求权限查看](https://google.github.io/modernstorage/permissions/#request-storage-permissions)
 */
fun SAFHelper.hasReadAccessByAllApps(types: List<StoragePermissions.FileType>): Boolean =
    storagePermissions.hasAccess(
        StoragePermissions.Action.READ,
        types,
        StoragePermissions.CreatedBy.AllApps
    )

/**
 * 有写权限自然有读权限
 * [请求权限查看](https://google.github.io/modernstorage/permissions/#request-storage-permissions)
 */
fun SAFHelper.hasWriteAccessByAllApps(types: List<StoragePermissions.FileType>): Boolean =
    storagePermissions.hasAccess(
        StoragePermissions.Action.READ_AND_WRITE,
        types,
        StoragePermissions.CreatedBy.AllApps
    )
