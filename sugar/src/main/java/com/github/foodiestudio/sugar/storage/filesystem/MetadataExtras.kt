package com.github.foodiestudio.sugar.storage.filesystem

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