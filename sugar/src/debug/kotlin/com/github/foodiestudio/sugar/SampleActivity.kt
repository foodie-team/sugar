package com.github.foodiestudio.sugar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel

// 主要测试下 MediaStore 以及 Document 的移动
class SampleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Content()
        }
    }
}

enum class Page {
    Home, MediaStoreTest, SaveToDownload
}

@Composable
private fun Content(viewModel: SampleViewModel = viewModel()) {
    var page by remember {
        mutableStateOf(Page.Home)
    }
    when (page) {
        Page.Home -> {
            // 没有测试数据
            Home(viewModel = viewModel)
        }

        Page.MediaStoreTest -> {
            MediaStoreTest(viewModel)
        }

        Page.SaveToDownload -> {
            SaveFileToDownload(viewModel)
        }
    }
}

/**
 * 准备测试文件：
 * - 视频：video.mp4
 * - 图片：image.png
 *
 * 如果没有准备好的话，需要先手动选择
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Home(viewModel: SampleViewModel) {
    Column {
        Row {

        }
        if (viewModel.isPrepared) {
            ListItem {
                Text("Media test")
            }
        }
    }
}