package com.github.foodiestudio.sugar.recyclerView

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView

/**
 * 适用于绘制分类的头部，仅当 [shouldShowHeader] 情况满足的情况下才会展示 [header]
 */
internal class FooterItemDecoration(
    private val footer: View,
    private val shouldShowFooter: (Int) -> Boolean
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val itemPosition = parent.getChildAdapterPosition(view)
        if (shouldShowFooter(itemPosition)) {
            footer.measure(
                View.MeasureSpec.makeMeasureSpec(parent.measuredWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(
                    View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED
                )
            )
            outRect.bottom = footer.measuredHeight
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        footer.layout(parent.left, 0, parent.right, footer.measuredHeight)
        parent.children.forEach {
            val position = parent.getChildAdapterPosition(it)
            if (position != RecyclerView.NO_POSITION && shouldShowFooter(position)) {
                c.save()
                c.translate(0f, it.y + it.measuredHeight)
                footer.draw(c)
                c.restore()
            }
        }
    }
}