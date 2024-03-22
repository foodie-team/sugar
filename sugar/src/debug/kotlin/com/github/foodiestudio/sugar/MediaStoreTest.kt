package com.github.foodiestudio.sugar

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.foodiestudio.sugar.notification.toast
import com.github.foodiestudio.sugar.storage.filesystem.absoluteFilePath
import com.github.foodiestudio.sugar.storage.filesystem.displayName
import com.github.foodiestudio.sugar.storage.filesystem.mimeType
import okio.FileMetadata

/**
 * 测试点
 * 0. 准备测试视频，可以使用 SAF 的方式选择
 * 1. 写入：导入视频到 Movie 文件夹下，期望出现 ~/Movies/sugar/video.mp4
 * 2. 读取：拿图片测试作为代表
 *  - 类似与写入测试，将测试图片复制到 ~/Pictures/sugar/img.png
 *  - 继续读取这个图片（需要知道这个 uri，可以直接拿上一步创建时的 media uri）
 * 3. 显示 metadata 信息：检查是否无误
 * 4. 移动：将第二步的移动到『输入 的』相对目录下
 * 5. 重命名：将测试图片的名字修改为输入的
 * 6. 显示缩略图：加载测试视频的封面
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun MediaStoreTest(
    modifier: Modifier,
    viewModel: SampleViewModel,
) {
    val context = LocalContext.current
    var exportedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var exportedVideoUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var fileMetadata by remember {
        mutableStateOf<FileMetadata?>(null)
    }

    Column(modifier) {
        ListItem(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    exportedVideoUri = viewModel.exportVideo(context)
                    context.toast("期望出现 ~/Movies/sugar/video.mp4")
                } else {
                    TODO("Android 10 以下版本暂未测试")
                }
            }, text = {
            Text(text = "写入测试")
        }, secondaryText = {
            Text(text = "将测试视频写入到 Movies 文件夹下")
        }, trailing = {
            exportedVideoUri?.let {
                Image(
                    viewModel.loadThumbnail(context, it).asImageBitmap(),
                    contentDescription = null
                )
            }
        })

        ListItem(modifier = Modifier.clickable {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                exportedImageUri = viewModel.exportImage(context).also {
                    // content://media/external_primary/images/media/1000060604
                    Log.d("MediaStoreTest", "exportedImageUri: $it")
                }
                context.toast("期望出现 ~/Pictures/sugar/img.png")
            } else {
                TODO("Android 10 以下版本暂未测试")
            }
        }, trailing = {
            AsyncImage(
                model = exportedImageUri,
                contentDescription = "",
                modifier = Modifier.size(64.dp)
            )
        }, text = {
            Text(text = "写入测试")
        }, secondaryText = {
            Text(text = "将测试图片写入到 Pictures 文件夹下")
        })
        if (exportedImageUri != null) {
            ListItem(modifier = Modifier.clickable {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    fileMetadata = viewModel.readImageMetadata(context, exportedImageUri!!)
                } else {
                    TODO("Android 10 以下版本暂未测试")
                }
            }, icon = {},
                text = {
                    Text(text = "Metadata 测试")
                }, secondaryText = {
                    Text(text = "点击查看详情")
                })
            ListItem(modifier = Modifier.clickable {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    viewModel.moveImage(context, exportedImageUri!!, "DCIM/sugar")
                    context.toast("期望出现 ~/DCIM/sugar/img.png")
                } else {
                    TODO("Android 10 以下版本暂未测试")
                }
            }, icon = {},
                text = {
                    Text(text = "移动测试")
                }, secondaryText = {
                    Text(text = "移动到 DCIM 文件夹下")
                })
            ListItem(modifier = Modifier.clickable {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    viewModel.renameImage(context, exportedImageUri!!, "newImg.png")
                    context.toast("期望出现 sugar/newImg.png")
                } else {
                    TODO("Android 10 以下版本暂未测试")
                }
            }, icon = {},
                text = {
                    Text(text = "重命名测试")
                }, secondaryText = {
                    Text(text = "img.png -> newImg.png")
                })
        }
    }

    if (fileMetadata != null) {
        MetadataDialog(fileMetadata = fileMetadata!!, onDismiss = {
            fileMetadata = null
        })
    }
}

@Composable
private fun MetadataDialog(
    modifier: Modifier = Modifier,
    fileMetadata: FileMetadata,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        text = {
            OutlinedTextField(
                value = """
                displayName     : ${fileMetadata.displayName}
                size            : ${fileMetadata.size} bytes
                mimeType        : ${fileMetadata.mimeType}
                absoluteFilePath: ${fileMetadata.absoluteFilePath}
            """.trimIndent(),
                onValueChange = {},
            )
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "确定")
            }
        },
    )
}