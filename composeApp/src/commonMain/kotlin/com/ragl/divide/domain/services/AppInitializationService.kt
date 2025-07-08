package com.ragl.divide.domain.services

import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
interface AppInitializationService {
    val isInitializing: kotlinx.coroutines.flow.StateFlow<Boolean>
    val startAtLogin: kotlinx.coroutines.flow.StateFlow<Boolean>
    
    suspend fun initializeApp()
}