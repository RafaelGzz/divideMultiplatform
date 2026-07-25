package com.ragl.divide.domain.services

import io.mockative.Mockable
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Mockable
interface AppInitializationService {
    val isInitializing: kotlinx.coroutines.flow.StateFlow<Boolean>
    val startAtLogin: kotlinx.coroutines.flow.StateFlow<Boolean>
    
    suspend fun initializeApp()
}