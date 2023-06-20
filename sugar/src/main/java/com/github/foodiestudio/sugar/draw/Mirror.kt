package com.github.foodiestudio.sugar.draw

import androidx.compose.ui.Modifier
import androidx.compose.runtime.Stable
import androidx.compose.ui.draw.scale

/**
 * Flip horizontally
 */
@Stable
fun Modifier.mirror() = scale(scaleX = -1f, scaleY = 1f)