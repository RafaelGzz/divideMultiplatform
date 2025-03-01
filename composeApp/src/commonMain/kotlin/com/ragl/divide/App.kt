package com.ragl.divide

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import com.ragl.divide.ui.screens.signIn.SignInScreen
import com.ragl.divide.ui.theme.DivideTheme
import org.koin.compose.KoinContext
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun App() {

    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        GoogleAuthProvider.create(
            credentials = GoogleAuthCredentials(
                serverId = "136615745370-2h4gflq2jv0u176mhbpu0ke5fei3cb4t.apps.googleusercontent.com"
            )
        )
        loaded = true
    }

    DivideTheme {
        KoinContext {
            AnimatedVisibility(visible = loaded, enter = fadeIn()) {
                Navigator(
                    SignInScreen()
                )
            }
        }
    }
}