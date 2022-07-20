package com.github.foodiestudio.sugar.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher

/**
 * An AccessibilityService that is also a [LifecycleOwner]
 */
abstract class LifecycleAccessibilityService : AccessibilityService(), LifecycleOwner {
    private val mDispatcher = ServiceLifecycleDispatcher(this)

    @CallSuper
    override fun onCreate() {
        mDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
    }

    @CallSuper
    override fun onServiceConnected() {
        mDispatcher.onServicePreSuperOnBind()
        super.onServiceConnected()
    }

    @CallSuper
    override fun onDestroy() {
        mDispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override fun getLifecycle(): Lifecycle = mDispatcher.lifecycle

}