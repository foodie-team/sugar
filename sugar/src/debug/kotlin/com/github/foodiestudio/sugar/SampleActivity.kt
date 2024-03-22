package com.github.foodiestudio.sugar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis

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
            // 准备测试数据
            Home(viewModel = viewModel, onNavigateNext = { page = Page.MediaStoreTest })
        }

        Page.MediaStoreTest -> {
            BackHandler {
                page = Page.Home
            }
            Scaffold(
                floatingActionButton = {
                    if (viewModel.isImagePrepared && viewModel.isVideoPrepared) {
                        FloatingActionButton(onClick = { page = Page.SaveToDownload }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                },
                content = {
                    MediaStoreTest(modifier = Modifier.padding(it), viewModel)
                }
            )
        }

        Page.SaveToDownload -> {
            BackHandler {
                page = Page.MediaStoreTest
            }
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
@Composable
private fun Home(viewModel: SampleViewModel, onNavigateNext: () -> Unit) {
    var isImagePrepared by remember {
        mutableStateOf(viewModel.isImagePrepared)
    }
    var isVideoPrepared by remember {
        mutableStateOf(viewModel.isVideoPrepared)
    }
    val imageSelector =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            it?.run(viewModel::updateImagePath)
            isImagePrepared = viewModel.isImagePrepared
        }
    val videoSelector =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            it?.run(viewModel::updateVideoPath)
            isVideoPrepared = viewModel.isVideoPrepared
        }
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            if (isImagePrepared && isVideoPrepared) {
                FloatingActionButton(onClick = onNavigateNext) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        },
        content = { contentPadding ->
            Column(Modifier.padding(contentPadding)) {
                MenuItem(
                    text = viewModel.testImagePath.toString().takeIf { isImagePrepared }
                        ?: "请先选择图片",
                    modifier = Modifier.weight(1f),
                    imageRequest = ImageRequest.Builder(context)
                        .data(viewModel.testImagePath.toFile())
                        .build().takeIf { isImagePrepared },
                ) {
                    imageSelector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                MenuItem(
                    text = viewModel.testVideoPath.toString().takeIf { isVideoPrepared }
                        ?: "请先选择视频",
                    modifier = Modifier.weight(1f),
                    imageRequest = ImageRequest.Builder(context)
                        .data(viewModel.testVideoPath.toFile())
                        .videoFrameMillis(1000)
                        .decoderFactory { result, options, _ ->
                            VideoFrameDecoder(
                                result.source,
                                options
                            )
                        }
                        .build().takeIf { isVideoPrepared },
                ) {
                    videoSelector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                }
            }
        }
    )
}

@Composable
private fun MenuItem(
    modifier: Modifier = Modifier,
    text: String,
    imageRequest: ImageRequest?,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (imageRequest != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .size(64.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }
            Text(
                text = text,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}