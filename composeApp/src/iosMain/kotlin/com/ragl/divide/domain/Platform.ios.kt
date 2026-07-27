package com.ragl.divide.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

class IOSPlatform : Platform {
    override val name: String = "iOS"
}

actual fun getPlatform(): Platform = IOSPlatform()

class IOSAppLifecycleHandler : AppLifecycleHandler {
    private val _isAppInForeground = MutableStateFlow(true)
    private var lastPauseTime: Long = 0L
    private val backgroundThreshold = 5 * 60 * 1000L // 5 minutos en milisegundos

    private fun currentEpochMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }

    override fun onAppResume() {
        val currentTime = currentEpochMillis()
        val wasInBackgroundLongTime = (currentTime - lastPauseTime) > backgroundThreshold

        _isAppInForeground.value = true

        if (wasInBackgroundLongTime && lastPauseTime > 0L) {
            // Callback logic if needed
        }
    }

    override fun onAppPause() {
        lastPauseTime = currentEpochMillis()
        _isAppInForeground.value = false
    }

    override val isAppInForeground: StateFlow<Boolean> = _isAppInForeground
}