package com.ragl.divide

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

// Interfaz para manejar el ciclo de vida de la aplicación
interface AppLifecycleHandler {
    fun onAppResume()
    fun onAppPause()
    val isAppInForeground: StateFlow<Boolean>
}

// Implementación por defecto
class DefaultAppLifecycleHandler : AppLifecycleHandler {
    private val _isAppInForeground = MutableStateFlow(true)
    
    override fun onAppResume() {
        _isAppInForeground.value = true
    }
    
    override fun onAppPause() {
        _isAppInForeground.value = false
    }
    
    override val isAppInForeground: StateFlow<Boolean> = _isAppInForeground
}