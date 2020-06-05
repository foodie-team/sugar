package com.github.foodie_team.sugar

import android.content.res.Resources
import android.util.TypedValue

/**
 * 返回相应的 Px 大小，相当于原来的 dpToPx
 * 注意，为了简化使用，这里用的是系统的,对于负数也不做特殊处理
 */
val Number.dp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics)

val Number.sp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics)