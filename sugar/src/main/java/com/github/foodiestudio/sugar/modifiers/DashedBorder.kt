package com.github.foodiestudio.sugar.modifiers

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.Dp

@ExperimentalComposeUiApi
fun Modifier.dashedBorder(strokeWidth: Dp, color: Color, cornerRadius: Dp): Modifier =
    this then DashedBorderElement(
        color = color,
        strokeWidth = strokeWidth,
        cornerRadius = cornerRadius
    )

@ExperimentalComposeUiApi
private data class DashedBorderElement(
    val strokeWidth: Dp,
    val color: Color,
    val cornerRadius: Dp
) : ModifierNodeElement<DashedBorderNode>() {
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
    var strokeWidth: Dp,
    var cornerRadius: Dp
) : DrawModifierNode, Modifier.Node() {

    override fun ContentDrawScope.draw() {
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 10f)
        )
        drawRoundRect(
            color = color,
            style = stroke,
            cornerRadius = CornerRadius(cornerRadius.toPx())
        )
        drawContent()
    }
}