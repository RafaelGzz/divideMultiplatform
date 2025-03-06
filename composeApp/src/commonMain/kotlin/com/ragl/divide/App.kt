package com.ragl.divide

import ContentWithMessageBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.CrossfadeTransition
import cafe.adriel.voyager.transitions.FadeTransition
import cafe.adriel.voyager.transitions.SlideTransition
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import com.ragl.divide.data.repositories.PreferencesRepository
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.home.HomeScreen
import com.ragl.divide.ui.screens.signIn.SignInScreen
import com.ragl.divide.ui.theme.DivideTheme
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import rememberMessageBarState

@Composable
fun App() {

    var loaded by remember { mutableStateOf(false) }
    val userViewModel: UserViewModel = koinInject()
    val darkModeState by userViewModel.isDarkMode.collectAsState()
    val startAtLogin by userViewModel.startAtLogin
    val messageBarState = rememberMessageBarState()
    val errorState by userViewModel.errorState.collectAsState()
    val successState by userViewModel.successState.collectAsState()

    LaunchedEffect(Unit) {
        GoogleAuthProvider.create(
            credentials = GoogleAuthCredentials(
                serverId = "136615745370-2h4gflq2jv0u176mhbpu0ke5fei3cb4t.apps.googleusercontent.com"
            )
        )
        loaded = true
    }

    LaunchedEffect(errorState) {
        errorState?.let { errorMessage ->
            messageBarState.addError(Exception(errorMessage))
            userViewModel.clearError()
        }
    }

    LaunchedEffect(successState) {
        successState?.let { successMessage ->
            messageBarState.addSuccess(successMessage)
        }
    }

    DivideTheme(darkTheme = darkModeState?.toBoolean() ?: isSystemInDarkTheme()) {
        KoinContext {
            AnimatedVisibility(visible = loaded, enter = fadeIn()) {
                ContentWithMessageBar(
                    messageBarState = messageBarState,
                    position = MessageBarPosition.BOTTOM,
                    successContentColor = MaterialTheme.colorScheme.onPrimary,
                    successContainerColor = MaterialTheme.colorScheme.primary,
                    errorContentColor = MaterialTheme.colorScheme.onError,
                    errorContainerColor = MaterialTheme.colorScheme.error,
                    showCopyButton = false,
                    errorMaxLines = 3,
                    successMaxLines = 3
                ) {
                    Navigator(
                        if (startAtLogin)
                            SignInScreen()
                        else
                            HomeScreen()
                    ) { navigator ->
                        FadeTransition(navigator)
                    }
                }
            }
        }
    }
}