package com.github.foodiestudio.sugar.storage.filesystem

import okio.FileMetadata

/**
 * https://github.com/google/modernstorage/blob/main/storage/src/main/java/com/google/modernstorage/storage/MetadataExtras.kt
 */
internal object MetadataExtras {
    @JvmInline
    value class DisplayName(val value: String)

    @JvmInline
    value class MimeType(val value: String)

    @JvmInline
    value class FilePath(val value: String)
}

val FileMetadata.displayName: String
    get() = extra(MetadataExtras.DisplayName::class)!!.value

val FileMetadata.mimeType: String?
    get() = extra(MetadataExtras.MimeType::class)?.value

/**
 * 对于 MediaFile 来说，肯定不会为 null，否则不一定
 *
 * 注意：介于 uri 也可以写文件，所以可能真的不需要这个 API
 */
val FileMetadata.absoluteFilePath: String?
    get() = extra(MetadataExtras.FilePath::class)?.value