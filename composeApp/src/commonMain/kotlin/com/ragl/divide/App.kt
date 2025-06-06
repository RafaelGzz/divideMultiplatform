package com.ragl.divide

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.transitions.FadeTransition
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import com.ragl.divide.ui.components.ContentWithMessageBar
import com.ragl.divide.ui.components.MessageBarPosition
import com.ragl.divide.ui.components.rememberMessageBarState
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.main.MainScreen
import com.ragl.divide.ui.screens.signIn.SignInScreen
import com.ragl.divide.ui.theme.DivideTheme
import com.ragl.divide.ui.utils.logMessage
import kotlinx.datetime.Clock
import org.koin.compose.koinInject

@Composable
fun App() {

    var loaded by remember { mutableStateOf(false) }
    val userViewModel: UserViewModel = koinInject()
    val darkModeState by userViewModel.isDarkMode.collectAsState()
    val startAtLogin by userViewModel.startAtLogin
    val isInitializing by userViewModel.isInitializing
    val messageBarState = rememberMessageBarState()
    val errorState by userViewModel.errorState.collectAsState()
    val successState by userViewModel.successState.collectAsState()
    val appState by userViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        val startTime = Clock.System.now().toEpochMilliseconds()
        GoogleAuthProvider.create(
            credentials = GoogleAuthCredentials(
                serverId = "136615745370-2h4gflq2jv0u176mhbpu0ke5fei3cb4t.apps.googleusercontent.com"
            )
        )
        loaded = true
        val timeTaken = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("App", "GoogleAuthProvider loaded in $timeTaken ms")
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
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // Solo mostrar la UI principal cuando esté cargada Y NO esté inicializando
            AnimatedVisibility(visible = loaded && !isInitializing, enter = fadeIn()) {
                ContentWithMessageBar(
                    messageBarState = messageBarState,
                    position = MessageBarPosition.BOTTOM,
                    successContentColor = MaterialTheme.colorScheme.onPrimary,
                    successContainerColor = MaterialTheme.colorScheme.primary,
                    errorContentColor = MaterialTheme.colorScheme.onError,
                    errorContainerColor = MaterialTheme.colorScheme.error,
                    maxLines = 3,
                    autoDismiss = true,
                    autoDismissDuration = 4000L,
                    showDismissButton = true
                ) {
                    Navigator(
                        if (startAtLogin)
                            SignInScreen()
                        else
                            MainScreen(),
                        NavigatorDisposeBehavior(true, true)
                    ) { navigator ->
                        FadeTransition(navigator)
                    }
                }
            }

            // Indicador de carga global
            if (appState.isLoading || isInitializing || !loaded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f))
                        .clickable(
                            enabled = true,
                            onClick = {},
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}