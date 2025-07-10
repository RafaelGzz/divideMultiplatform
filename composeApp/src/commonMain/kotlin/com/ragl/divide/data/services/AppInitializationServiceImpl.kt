package com.ragl.divide.data.services

import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import com.ragl.divide.domain.AppLifecycleHandler
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.AppInitializationService
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.utils.logMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class AppInitializationServiceImpl(
    private val userRepository: UserRepository,
    private val userStateHolder: UserStateHolder,
    private val appLifecycleHandler: AppLifecycleHandler
) : AppInitializationService {

    private val _isInitializing = MutableStateFlow(true)
    override val isInitializing = _isInitializing.asStateFlow()

    private val _startAtLogin = MutableStateFlow(true)
    override val startAtLogin = _startAtLogin.asStateFlow()

    override suspend fun initializeApp() {
        val startTime = Clock.System.now().toEpochMilliseconds()
        GoogleAuthProvider.create(
            credentials = GoogleAuthCredentials(
                serverId = "136615745370-2h4gflq2jv0u176mhbpu0ke5fei3cb4t.apps.googleusercontent.com"
            )
        )
        try {
            if (userRepository.getFirebaseUser() != null) {
                if (userRepository.isEmailVerified()) {
                    _startAtLogin.value = false
                    logMessage("AppInitializationService", "User is verified, getting data")
                    userStateHolder.refreshUser()
                } else {
                    userRepository.signOut()
                    _startAtLogin.value = true
                    logMessage("AppInitializationService", "User is not verified, signing out")
                }
            } else {
                _startAtLogin.value = true
                logMessage("AppInitializationService", "User is not logged in")
            }
        } catch (e: Exception) {
            _startAtLogin.value = true
            logMessage("AppInitializationService", "Error during initialization: ${e.message}")
        } finally {
            _isInitializing.value = false
        }
        val timeTaken = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("AppInitializationService", "Initialization completed in $timeTaken ms")

        appLifecycleHandler.isAppInForeground.collect { isInForeground ->
            if (isInForeground && !startAtLogin.value && !isInitializing.value) {
                logMessage("AppInitializationService", "App regresó al foreground, actualizando datos")
                userStateHolder.refreshUser()
            }
        }
    }
} 