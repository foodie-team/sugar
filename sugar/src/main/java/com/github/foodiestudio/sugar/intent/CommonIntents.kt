package com.github.foodiestudio.sugar.intent

import android.content.Intent

object CommonIntents {
    /**
     * 行为返回到首页，相当于手动点击了 Home
     * 常用于首页，避免用户点击返回后直接杀掉，导致下次为冷启动。
     */
    val HOME_SCREEN: Intent
        get() = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
}
