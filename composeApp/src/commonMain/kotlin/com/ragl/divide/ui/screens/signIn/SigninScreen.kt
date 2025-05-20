package com.ragl.divide.ui.screens.signIn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.TabRow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.home.HomeScreen
import com.ragl.divide.ui.utils.DivideTextField
import com.ragl.divide.ui.utils.Strings
import dev.gitlive.firebase.auth.FirebaseUser
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.app_name
import dividemultiplatform.composeapp.generated.resources.confirm_password_text
import dividemultiplatform.composeapp.generated.resources.connect_with_google
import dividemultiplatform.composeapp.generated.resources.connect_with_social_media
import dividemultiplatform.composeapp.generated.resources.email_address_text
import dividemultiplatform.composeapp.generated.resources.log_in
import dividemultiplatform.composeapp.generated.resources.password_text
import dividemultiplatform.composeapp.generated.resources.resend_verification
import dividemultiplatform.composeapp.generated.resources.sign_up
import dividemultiplatform.composeapp.generated.resources.username
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class SignInScreen : Screen {

    @Composable
    override fun Content() {
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val tabs = listOf(Res.string.log_in, Res.string.sign_up)
        val pagerState = rememberPagerState { tabs.size }
        val scope = rememberCoroutineScope()
        LaunchedEffect(pagerState.currentPage) {
            selectedTabIndex = pagerState.currentPage
        }
        val navigator = LocalNavigator.currentOrThrow
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()
        val strings: Strings = koinInject()
        var showResendButton by remember { mutableStateOf(false) }
        Scaffold(
            backgroundColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.statusBarsPadding()
        ) { pv ->
            Column(Modifier.fillMaxSize().padding(pv)) {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = stringResource(resource = Res.string.app_name),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                                selectedTabIndex = index
                            },
                            text = {
                                Text(
                                    text = stringResource(title),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.onSurface,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.height(64.dp)
                        )
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = false
                ) { pagerIndex ->
                    when (pagerIndex) {
                        0 -> Login(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 20.dp)
                                .verticalScroll(rememberScrollState())
                                .fillMaxHeight(),
                            onLoginButtonClick = { email, password ->
                                userViewModel.signInWithEmailAndPassword(
                                    email,
                                    password,
                                    { navigator.replace(HomeScreen()) },
                                    { error ->
                                        if (error.message == strings.getEmailNotVerified()) {
                                            navigator.push(EmailVerificationScreen())
                                        } else {
                                            userViewModel.handleError(error)
                                        }
                                    })
                            },
                            onGoogleSignIn = { result ->
                                userViewModel.signInWithGoogle(
                                    result,
                                    { navigator.replace(HomeScreen()) },
                                    { userViewModel.handleError(it) })
                            },
                            showResendButton = showResendButton,
                            updateShowResendButton = { showResendButton = it }
                        )

                        1 -> SignUp(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 20.dp)
                                .verticalScroll(rememberScrollState())
                                .fillMaxHeight(),
                            onSignUpButtonClick = { email, password, username ->
                                userViewModel.signUpWithEmailAndPassword(
                                    email,
                                    password,
                                    username
                                )
                            },
                            onGoogleSignIn = { result ->
                                userViewModel.signInWithGoogle(
                                    result,
                                    { navigator.replace(HomeScreen()) },
                                    { userViewModel.handleError(it) })
                            }
                        )
                    }
                }
            }
        }
    }


    @Composable
    fun SocialMediaRow(
        onGoogleSignIn: (Result<FirebaseUser?>) -> Unit
    ) {
        Column {
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .3f))
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.connect_with_social_media),
                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface)
            )
            Spacer(modifier = Modifier.height(20.dp))
            GoogleButtonUiContainerFirebase(
                linkAccount = false,
                onResult = { result ->
                    onGoogleSignIn(result)
                }
            ) {
                GoogleSignInButton(
                    onClick = { this.onClick() },
                    text = stringResource(Res.string.connect_with_google),
                    shape = ShapeDefaults.Medium,
                    modifier = Modifier.height(50.dp).fillMaxWidth()
                )
            }
        }
    }

    @Composable
    fun Login(
        modifier: Modifier = Modifier,
        onLoginButtonClick: (String, String) -> Unit,
        onGoogleSignIn: (Result<FirebaseUser?>) -> Unit,
        showResendButton: Boolean,
        updateShowResendButton: (Boolean) -> Unit
    ) {
        val vm: LogInViewModel = koinScreenModel<LogInViewModel>()
        val strings: Strings = koinInject()
        val navigator = LocalNavigator.currentOrThrow
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()

        Column(
            modifier = modifier
        ) {
            DivideTextField(
                label = stringResource(Res.string.email_address_text),
                input = vm.email,
                error = vm.emailError,
                imeAction = ImeAction.Next,
                onValueChange = { 
                    vm.updateEmail(it)
                    updateShowResendButton(false)
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            DivideTextField(
                label = stringResource(Res.string.password_text),
                input = vm.password,
                error = vm.passwordError,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                onValueChange = { 
                    vm.updatePassword(it)
                    updateShowResendButton(false)
                },
                onAction = { if (vm.isFieldsValid()) onLoginButtonClick(vm.email, vm.password) },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LoginButton(
                label = stringResource(Res.string.log_in),
                onClick = { 
                    if (vm.isFieldsValid()) {
                        onLoginButtonClick(vm.email, vm.password)
                        updateShowResendButton(false)
                    }
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )
            if (showResendButton) {
                TextButton(
                    onClick = {
                        userViewModel.resendVerificationEmail()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.resend_verification),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            SocialMediaRow(onGoogleSignIn)
        }
    }

    @Composable
    fun SignUp(
        modifier: Modifier = Modifier,
        onSignUpButtonClick: (String, String, String) -> Unit,
        onGoogleSignIn: (Result<FirebaseUser?>) -> Unit
    ) {
        val vm: SignUpViewModel = koinScreenModel<SignUpViewModel>()
        Column(
            modifier = modifier
        ) {
            DivideTextField(
                label = stringResource(Res.string.email_address_text),
                input = vm.email,
                error = vm.emailError,
                imeAction = ImeAction.Next,
                onValueChange = { vm.updateEmail(it) },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            DivideTextField(
                label = stringResource(Res.string.username),
                input = vm.username,
                error = vm.usernameError,
                imeAction = ImeAction.Next,
                onValueChange = { vm.updateUsername(it) },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            DivideTextField(
                label = stringResource(Res.string.password_text),
                input = vm.password,
                error = vm.passwordError,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
                //isPassword = true,
                onValueChange = { vm.updatePassword(it) },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            DivideTextField(
                label = stringResource(Res.string.confirm_password_text),
                input = vm.passwordConfirm,
                error = vm.passwordConfirmError,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                //isPassword = true,
                onValueChange = { vm.updatePasswordConfirm(it) },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LoginButton(
                label = stringResource(Res.string.sign_up),
                onClick = {
                    if (vm.isFieldsValid()) onSignUpButtonClick(
                        vm.email,
                        vm.password,
                        vm.username
                    )
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )
            SocialMediaRow(onGoogleSignIn)
        }
    }


    @Composable
    fun LoginButton(
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
        label: String
    ) {
        Button(
            onClick = { onClick() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            shape = ShapeDefaults.ExtraSmall,
            modifier = modifier.clip(ShapeDefaults.Medium)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary
                    )
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
