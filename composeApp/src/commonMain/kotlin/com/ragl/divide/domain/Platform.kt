package com.ragl.divide.domain

import kotlinx.coroutines.flow.StateFlow

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

interface AppLifecycleHandler {
    fun onAppResume()
    fun onAppPause()
    val isAppInForeground: StateFlow<Boolean>
}