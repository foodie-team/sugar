package com.github.foodiestudio.sugar.modifiers

import androidx.compose.ui.Modifier

/**
 * only If [condition] matched, then [action] will be applied.
 */
inline fun Modifier.thenIf(condition: Boolean, action: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        action(this)
    } else {
        this
    }
}