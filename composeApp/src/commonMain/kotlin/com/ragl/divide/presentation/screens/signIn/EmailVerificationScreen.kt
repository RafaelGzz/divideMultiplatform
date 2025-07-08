package com.ragl.divide.presentation.screens.signIn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.domain.repositories.PreferencesRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.screens.main.MainScreen
import com.ragl.divide.presentation.screens.onboarding.OnboardingScreen
import com.ragl.divide.presentation.utils.Strings
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.check_email_for_verification
import dividemultiplatform.composeapp.generated.resources.email_not_verified
import dividemultiplatform.composeapp.generated.resources.resend_verification
import dividemultiplatform.composeapp.generated.resources.verify_email
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class EmailVerificationScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authViewModel: AuthViewModel = koinScreenModel<AuthViewModel>()
        val scope = rememberCoroutineScope()
        val countdown by authViewModel.countdown.collectAsState()
        val strings: Strings = koinInject()
        val userStateHolder: UserStateHolder = koinInject()
        val preferencesRepository: PreferencesRepository = koinInject()
        val isFirstTime by preferencesRepository.isFirstTimeFlow.collectAsState(false)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .clip(ShapeDefaults.Medium)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(Res.string.email_not_verified),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    stringResource(Res.string.check_email_for_verification),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Button(
                    onClick = authViewModel::resendVerificationEmail,
                    enabled = countdown == 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = if (countdown == 0) stringResource(Res.string.resend_verification) else "${
                            stringResource(
                                Res.string.resend_verification
                            )
                        } ($countdown)",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        scope.launch {
                            if (authViewModel.isEmailVerified()) {
                                if (isFirstTime) {
                                    navigator.replaceAll(OnboardingScreen())
                                } else {
                                    userStateHolder.refreshUser()
                                    navigator.replaceAll(MainScreen())
                                }
                            } else {
                                authViewModel.handleError(strings.getEmailNotVerified())
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.verify_email),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                TextButton(
                    onClick = {
                        scope.launch {
                            authViewModel.signOut {
                                navigator.pop()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        }
    }
} 