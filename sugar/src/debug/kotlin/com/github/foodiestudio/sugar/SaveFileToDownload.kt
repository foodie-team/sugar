package com.github.foodiestudio.sugar

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.foodiestudio.sugar.notification.toast

/**
 * 往 Downloads/ 下，保存任意文件，实际是属于 MediaStore，但又可以不是媒体文件，比如 Txt、PDF等
 *
 * 期望在 Download/sugar 目录下保存对应的文件
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun SaveFileToDownload(modifier: Modifier, viewModel: SampleViewModel) {
    val context = LocalContext.current
    Column(modifier) {
        ListItem(modifier = Modifier.clickable {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                viewModel.saveTextToDownload(context, "Hello world")
                context.toast("期望出现 Download/sugar/demo.txt")
            } else {
                TODO("Android 10 以下版本暂未测试")
            }
        }, text = {
            Text(text = "写入测试")
        }, secondaryText = {
            Text(text = "写入非 Media 文件到 Download 文件夹下")
        })
    }
}