package com.github.foodiestudio.sugar

import androidx.compose.runtime.Composable

/**
 * 测试点
 * 0. 准备测试视频，可以使用 SAF 的方式选择
 * 1. 写入：导入视频到 Movie 文件夹下，期望出现 ~/Movie/sugar/video.mp4
 * 2. 读取：拿图片测试作为代表
 *  - 类似与写入测试，将测试图片复制到 ~/Picture/sugar/img.png
 *  - 继续读取这个图片（需要知道这个 uri，这个 uri 要怎么获取？）
 * 3. 显示 metadata 信息：检查是否无误
 * 4. 移动：将第二步的移动到『输入的』相对目录下
 * 5. 重命名：将测试图片的名字修改为输入的
 * 6. 显示缩略图：加载测试视频的封面
 */
@Composable
internal fun MediaStoreTest(viewModel: SampleViewModel) {

}