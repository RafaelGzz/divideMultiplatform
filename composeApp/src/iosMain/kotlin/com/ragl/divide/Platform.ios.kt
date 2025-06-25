package com.ragl.divide

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class IOSPlatform : Platform {
    override val name: String = "iOS"
}

actual fun getPlatform(): Platform = IOSPlatform()

// Implementación específica de iOS para el manejo del ciclo de vida
// Por simplicidad, usamos una implementación básica sin listeners de notificaciones
class IOSAppLifecycleHandler : AppLifecycleHandler {
    private val _isAppInForeground = MutableStateFlow(true)
    private var lastPauseTime: Long = 0
    private val backgroundThreshold = 5 * 60 * 1000L // 5 minutos en milisegundos
    
    override fun onAppResume() {
        val currentTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val wasInBackgroundLongTime = (currentTime - lastPauseTime) > backgroundThreshold
        
        _isAppInForeground.value = true
        
        // Si estuvo en background por más del threshold, ejecutar callback
        if (wasInBackgroundLongTime && lastPauseTime > 0) {
            // Aquí se podría ejecutar lógica adicional si fuera necesario
        }
    }
    
    override fun onAppPause() {
        lastPauseTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        _isAppInForeground.value = false
    }
    
    override val isAppInForeground: StateFlow<Boolean> = _isAppInForeground
}