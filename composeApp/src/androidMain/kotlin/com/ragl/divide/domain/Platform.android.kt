package com.ragl.divide.domain

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

// Implementación específica de Android para el manejo del ciclo de vida
@OptIn(ExperimentalTime::class)
class AndroidAppLifecycleHandler(private val application: Application) : AppLifecycleHandler {
    private val _isAppInForeground = MutableStateFlow(true)
    private var lastPauseTime: Long = 0
    private val backgroundThreshold = 5 * 60 * 1000L // 5 minutos en milisegundos
    
    init {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            
            override fun onActivityStarted(activity: Activity) {}
            
            override fun onActivityResumed(activity: Activity) {
                val currentTime = Clock.System.now().toEpochMilliseconds()
                val timeInBackground = currentTime - lastPauseTime
                
                // Solo considerar que estuvo en background si pasó más del umbral de tiempo
                if (timeInBackground > backgroundThreshold) {
                    onAppResume()
                }
            }
            
            override fun onActivityPaused(activity: Activity) {
                lastPauseTime = Clock.System.now().toEpochMilliseconds()
                onAppPause()
            }
            
            override fun onActivityStopped(activity: Activity) {}
            
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
    
    override fun onAppResume() {
        _isAppInForeground.value = true
    }
    
    override fun onAppPause() {
        _isAppInForeground.value = false
    }
    
    override val isAppInForeground: StateFlow<Boolean> = _isAppInForeground
}