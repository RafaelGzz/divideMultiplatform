package com.ragl.divide.presentation.screens

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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.transitions.FadeTransition
import com.ragl.divide.data.services.AppStateServiceImpl
import com.ragl.divide.domain.repositories.PreferencesRepository
import com.ragl.divide.domain.services.AppInitializationService
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.components.ContentWithMessageBar
import com.ragl.divide.presentation.components.MessageBarPosition
import com.ragl.divide.presentation.components.rememberMessageBarState
import com.ragl.divide.presentation.screens.main.MainScreen
import com.ragl.divide.presentation.screens.signIn.SignInScreen
import com.ragl.divide.presentation.state.LocalThemeState
import com.ragl.divide.presentation.state.LocalUserState
import com.ragl.divide.presentation.theme.DivideTheme
import org.koin.compose.koinInject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun App() {
    val messageBarState = rememberMessageBarState()

    val preferencesRepository: PreferencesRepository = koinInject()
    val darkModeState by preferencesRepository.darkModeFlow.collectAsState(null)

    val userStateHolder: UserStateHolder = koinInject()
    val userState by userStateHolder.userState.collectAsState()

    val appInitializationService: AppInitializationService = koinInject()
    val startAtLogin by appInitializationService.startAtLogin.collectAsState()
    val isInitializing by appInitializationService.isInitializing.collectAsState()

    val appStateService: AppStateServiceImpl = koinInject()
    val errorState by appStateService.errorState.collectAsState()
    val successState by appStateService.successState.collectAsState()
    val isLoading by appStateService.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        appInitializationService.initializeApp()
    }

    LaunchedEffect(errorState) {
        errorState?.let { errorMessage ->
            messageBarState.addError(Exception(errorMessage))
            appStateService.clearError()
        }
    }

    LaunchedEffect(successState) {
        successState?.let { successMessage ->
            messageBarState.addSuccess(successMessage)
        }
    }
    CompositionLocalProvider(
        LocalThemeState provides darkModeState,
        LocalUserState provides userState
    ) {
        DivideTheme(darkTheme = darkModeState?.toBoolean() ?: isSystemInDarkTheme()) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            ) {
                AnimatedVisibility(!isInitializing, enter = fadeIn()) {
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

                if (isLoading || isInitializing) {
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
}