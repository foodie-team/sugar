package com.github.foodiestudio.sugar.modifiers

import androidx.annotation.Px
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@ExperimentalComposeUiApi
fun Modifier.dashedBorder(strokeWidth: Dp, color: Color, cornerRadiusDp: Dp): Modifier = composed {
    val density = LocalDensity.current
    this then DashedBorderElement(
        color = color,
        strokeWidth = density.run { strokeWidth.toPx() },
        cornerRadius = density.run { cornerRadiusDp.toPx() })
}

@ExperimentalComposeUiApi
private data class DashedBorderElement(
    val strokeWidth: Float,
    val color: Color,
    val cornerRadius: Float
) : ModifierNodeElement<DashedBorderNode>(inspectorInfo = {}) {
    override fun create(): DashedBorderNode = DashedBorderNode(
        color = color,
        strokeWidth = strokeWidth,
        cornerRadius = cornerRadius
    )

    override fun update(node: DashedBorderNode): DashedBorderNode {
        node.color = color
        node.cornerRadius = cornerRadius
        node.strokeWidth = strokeWidth
        return node
    }
}

@ExperimentalComposeUiApi
private class DashedBorderNode(
    var color: Color,
    @Px
    var strokeWidth: Float,
    @Px
    var cornerRadius: Float
) : DrawModifierNode, Modifier.Node() {

    override fun ContentDrawScope.draw() {
        val stroke = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 10f)
        )
        drawRoundRect(
            color = color,
            style = stroke,
            cornerRadius = CornerRadius(cornerRadius)
        )
        drawContent()
    }
}