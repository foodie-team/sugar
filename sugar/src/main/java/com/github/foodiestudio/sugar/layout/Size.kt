package com.github.foodiestudio.sugar.layout

import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * compose 里测量宽高是深度优先遍历视图树，逻辑也是很简单的，父节点将布局的最小和最大范围限制一路往下传递，遇到一些修改范围的 Modifier，
 * 则会产生新的范围限制，这个新的会继续往下传递。
 * 常见的 `Modifier.size` 是受父节点传递下来的最小和最大范围限制的，不会突破这个，但在一些场景下，想要突破这个限制
 * [查看更多](https://developer.android.google.cn/jetpack/compose/layouts/constraints-modifiers)
 */

// 忽略父节点传递下来的最小宽高的限制，最小值都重置为 0
@Stable
fun Modifier.forgetMinSize(alignment: Alignment = Alignment.Center): Modifier =
    wrapContentSize(alignment, false)

// 忽略父节点传递下来的最小宽的限制，最小值重置为 0
@Stable
fun Modifier.forgetMinWidth(alignment: Alignment.Horizontal = Alignment.CenterHorizontally): Modifier =
    wrapContentWidth(alignment, false)

@Stable
fun Modifier.forgetMinHeight(alignment: Alignment.Vertical = Alignment.CenterVertically): Modifier =
    wrapContentHeight(alignment, false)

// 忽略父节点传递下来的最小宽高的限制，重置为（0, +)
@Stable
fun Modifier.forgetMinAndMaxSize(alignment: Alignment): Modifier = wrapContentSize(alignment, true)

// 忽略父节点传递下来的宽高，最终这个节点会在父节点里居中显示
@Stable
fun Modifier.overrideSize(width: Dp, height: Dp) = this.then(requiredSize(width, height))

@Stable
fun Modifier.overrideWidth(width: Dp) = this.then(requiredWidth(width))
@Stable
fun Modifier.overrideWidthIn(minWidth: Dp, maxWidth: Dp) = this.then(requiredWidthIn(minWidth, maxWidth))

@Stable
fun Modifier.overrideHeight(height: Dp) = this.then(requiredHeight(height))
@Stable
fun Modifier.overrideHeightIn(minHeight: Dp, maxHeight: Dp) = this.then(requiredHeightIn(minHeight, maxHeight))



