package com.ragl.divide.presentation

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.google.firebase.FirebaseApp
import com.ragl.divide.domain.repositories.PreferencesRepository
import com.ragl.divide.presentation.screens.App
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        FirebaseApp.initializeApp(this)

        // Hacer que la barra de estado sea transparente
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )
        
        setContent {
            DivideApp(this)
        }
    }
}

@Composable
fun DivideApp(activity: ComponentActivity) {
    StatusBarEffect(activity)
    App()
}

@Composable
private fun StatusBarEffect(activity: ComponentActivity) {
    val preferencesRepository: PreferencesRepository = koinInject()
    val darkModeState by preferencesRepository.darkModeFlow.collectAsState(initial = null)
    val isDarkTheme = darkModeState?.toBoolean() ?: isSystemInDarkTheme()
    
    DisposableEffect(isDarkTheme) {
        WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = !isDarkTheme
        }
        
        onDispose { }
    }
}

