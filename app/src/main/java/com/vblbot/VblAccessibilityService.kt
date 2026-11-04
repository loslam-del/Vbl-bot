package com.vblbot

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class VblAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        VblServiceState.connected = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Intentionally does not inspect game content or perform input actions.
    }

    override fun onInterrupt() {
        VblServiceState.connected = false
    }

    override fun onDestroy() {
        VblServiceState.connected = false
        super.onDestroy()
    }
}

object VblServiceState {
    @Volatile var connected: Boolean = false
}
