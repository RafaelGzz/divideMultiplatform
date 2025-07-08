package com.ragl.divide.presentation.screens.signIn

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import com.ragl.divide.domain.repositories.PreferencesRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.components.DivideTextField
import com.ragl.divide.presentation.screens.main.MainScreen
import com.ragl.divide.presentation.screens.onboarding.OnboardingScreen
import com.ragl.divide.presentation.theme.DivideTheme
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.WindowWidthSizeClass
import com.ragl.divide.presentation.utils.getWindowWidthSizeClass
import dev.gitlive.firebase.auth.FirebaseUser
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.app_name
import dividemultiplatform.composeapp.generated.resources.app_subtitle
import dividemultiplatform.composeapp.generated.resources.back
import dividemultiplatform.composeapp.generated.resources.confirm_password_text
import dividemultiplatform.composeapp.generated.resources.connect_with_google
import dividemultiplatform.composeapp.generated.resources.email_address_text
import dividemultiplatform.composeapp.generated.resources.happy_bunch___party_of_three__1_
import dividemultiplatform.composeapp.generated.resources.ic_google
import dividemultiplatform.composeapp.generated.resources.log_in
import dividemultiplatform.composeapp.generated.resources.or
import dividemultiplatform.composeapp.generated.resources.password_text
import dividemultiplatform.composeapp.generated.resources.sign_up
import dividemultiplatform.composeapp.generated.resources.username
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

// Data classes para agrupar propiedades relacionadas
data class LoginState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null
)

data class LoginActions(
    val onEmailChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onEmailValidate: () -> Unit = {},
    val onPasswordValidate: () -> Unit = {},
    val isFieldsValid: () -> Boolean = { false }
)

data class SignUpState(
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val emailError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val passwordConfirmError: String? = null
)

data class SignUpActions(
    val onEmailChange: (String) -> Unit = {},
    val onUsernameChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onPasswordConfirmChange: (String) -> Unit = {},
    val onEmailValidate: () -> Unit = {},
    val onUsernameValidate: () -> Unit = {},
    val onPasswordValidate: () -> Unit = {},
    val onPasswordConfirmValidate: () -> Unit = {},
    val isFieldsValid: () -> Boolean = { false }
)

class SignInScreen : Screen {

    @OptIn(InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        var currentScreen by remember { mutableStateOf("main") } // "main", "login", "signup"
        val navigator = LocalNavigator.currentOrThrow
        val authViewModel: AuthViewModel = koinScreenModel<AuthViewModel>()
        val strings: Strings = koinInject()
        val windowSizeClass = getWindowWidthSizeClass()
        val coroutineScope = rememberCoroutineScope()

        val preferencesRepository: PreferencesRepository = koinInject()
        val isFirstTime by preferencesRepository.isFirstTimeFlow.collectAsState(false)

        val userStateHolder: UserStateHolder = koinInject()

        val onLoginClick = { 
            currentScreen = "login"
            authViewModel.resetLoginFields()
        }
        val onSignUpClick = {
            currentScreen = "signup"
            authViewModel.resetSignUpFields()
        }
        val onBackClick = { currentScreen = "main" }

        val onGoogleSignIn: (Result<FirebaseUser?>) -> Unit = { result ->
            authViewModel.signInWithGoogle(result = result) {
                coroutineScope.launch {
                    if (isFirstTime) {
                        navigator.replaceAll(OnboardingScreen())
                    } else {
                        userStateHolder.refreshUser()
                        navigator.replaceAll(MainScreen())
                    }
                }
            }
        }

        BackHandler(currentScreen != "main") {
            currentScreen = "main"
        }

        val onLoginButtonClick: (String, String) -> Unit = { email, password ->
            authViewModel.signInWithEmailAndPassword(
                email = email,
                password = password,
                onSuccess = {
                    coroutineScope.launch {
                        userStateHolder.refreshUser()
                        if (isFirstTime) {
                            navigator.replaceAll(OnboardingScreen())
                        } else {
                            navigator.replaceAll(MainScreen())
                        }
                    }
                },
                onFail = { error ->
                    if (error == strings.getEmailNotVerified()) {
                        navigator.push(EmailVerificationScreen())
                    } else {
                        authViewModel.handleError(error)
                    }
                })
        }

        val onSignUpButtonClick: (String, String, String) -> Unit = { email, password, username ->
            authViewModel.signUpWithEmailAndPassword(
                email = email,
                password = password,
                name = username
            )
        }

        // Crear estados y acciones
        val loginState = LoginState(
            email = authViewModel.loginEmail,
            password = authViewModel.loginPassword,
            emailError = authViewModel.loginEmailError,
            passwordError = authViewModel.loginPasswordError
        )

        val loginActions = LoginActions(
            onEmailChange = { authViewModel.updateLoginEmail(it) },
            onPasswordChange = { authViewModel.updateLoginPassword(it) },
            onEmailValidate = { authViewModel.validateLoginEmail() },
            onPasswordValidate = { authViewModel.validateLoginPassword() },
            isFieldsValid = { authViewModel.isLoginFieldsValid() }
        )

        val signUpState = SignUpState(
            email = authViewModel.signUpEmail,
            username = authViewModel.signUpUsername,
            password = authViewModel.signUpPassword,
            passwordConfirm = authViewModel.signUpPasswordConfirm,
            emailError = authViewModel.signUpEmailError,
            usernameError = authViewModel.signUpUsernameError,
            passwordError = authViewModel.signUpPasswordError,
            passwordConfirmError = authViewModel.signUpPasswordConfirmError
        )

        val signUpActions = SignUpActions(
            onEmailChange = { authViewModel.updateSignUpEmail(it) },
            onUsernameChange = { authViewModel.updateSignUpUsername(it) },
            onPasswordChange = { authViewModel.updateSignUpPassword(it) },
            onPasswordConfirmChange = { authViewModel.updateSignUpPasswordConfirm(it) },
            onEmailValidate = { authViewModel.validateSignUpEmail() },
            onUsernameValidate = { authViewModel.validateSignUpUsername() },
            onPasswordValidate = { authViewModel.validateSignUpPassword() },
            onPasswordConfirmValidate = { authViewModel.validateSignUpPasswordConfirm() },
            isFieldsValid = { authViewModel.isSignUpFieldsValid() }
        )

        when (windowSizeClass) {
            WindowWidthSizeClass.Compact -> {
                CompactLayout(
                    currentScreen = currentScreen,
                    onLoginClick = onLoginClick,
                    onSignUpClick = onSignUpClick,
                    onBackClick = onBackClick,
                    onLoginButtonClick = onLoginButtonClick,
                    onSignUpButtonClick = onSignUpButtonClick,
                    loginState = loginState,
                    loginActions = loginActions,
                    signUpState = signUpState,
                    signUpActions = signUpActions,
                    googleButton = {
                        GoogleButtonUiContainerFirebase(
                            linkAccount = false,
                            onResult = { result ->
                                onGoogleSignIn(result)
                            },
                        ) {
                            CustomGoogleButton { this@GoogleButtonUiContainerFirebase.onClick() }
                        }
                    }
                )
            }

            WindowWidthSizeClass.Medium,
            WindowWidthSizeClass.Expanded -> {
                ExpandedLayout(
                    currentScreen = currentScreen,
                    onLoginClick = onLoginClick,
                    onSignUpClick = onSignUpClick,
                    onBackClick = onBackClick,
                    onLoginButtonClick = onLoginButtonClick,
                    onSignUpButtonClick = onSignUpButtonClick,
                    loginState = loginState,
                    loginActions = loginActions,
                    signUpState = signUpState,
                    signUpActions = signUpActions,
                    googleButton = {
                        GoogleButtonUiContainerFirebase(
                            linkAccount = false,
                            onResult = { result ->
                                onGoogleSignIn(result)
                            },
                        ) {
                            CustomGoogleButton { this@GoogleButtonUiContainerFirebase.onClick() }
                        }
                    }
                )
            }
        }
    }

    @Preview
    @Composable
    fun CompactPreview() {
        DivideTheme {
            CompactLayout("main")
        }
    }

    @Preview
    @Composable
    fun ExpandedPreview() {
        DivideTheme {
            ExpandedLayout("main")
        }
    }

    @Composable
    private fun CompactLayout(
        currentScreen: String,
        onLoginClick: () -> Unit = {},
        onSignUpClick: () -> Unit = {},
        onBackClick: () -> Unit = {},
        onLoginButtonClick: (String, String) -> Unit = { _, _ -> },
        onSignUpButtonClick: (String, String, String) -> Unit = { _, _, _ -> },
        loginState: LoginState = LoginState(),
        loginActions: LoginActions = LoginActions(),
        signUpState: SignUpState = SignUpState(),
        signUpActions: SignUpActions = SignUpActions(),
        googleButton: @Composable () -> Unit = { CustomGoogleButton() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Section
                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    text = stringResource(resource = Res.string.app_name),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(Res.string.app_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Main Content Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(16.dp),
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith ExitTransition.None
                            },
                        ) { screen ->
                            when (screen) {
                                "main" -> MainAuthScreen(
                                    modifier = Modifier.wrapContentSize(),
                                    onLoginClick = onLoginClick,
                                    onSignUpClick = onSignUpClick,
                                    googleButton = googleButton
                                )

                                "login" -> Login(
                                    modifier = Modifier.wrapContentSize(),
                                    onLoginButtonClick = onLoginButtonClick,
                                    onBackClick = onBackClick,
                                    email = loginState.email,
                                    password = loginState.password,
                                    emailError = loginState.emailError,
                                    passwordError = loginState.passwordError,
                                    onEmailChange = loginActions.onEmailChange,
                                    onPasswordChange = loginActions.onPasswordChange,
                                    onEmailValidate = loginActions.onEmailValidate,
                                    onPasswordValidate = loginActions.onPasswordValidate,
                                    isFieldsValid = loginActions.isFieldsValid
                                )

                                "signup" -> SignUp(
                                    modifier = Modifier.wrapContentSize(),
                                    onSignUpButtonClick = onSignUpButtonClick,
                                    onBackClick = onBackClick,
                                    email = signUpState.email,
                                    username = signUpState.username,
                                    password = signUpState.password,
                                    passwordConfirm = signUpState.passwordConfirm,
                                    emailError = signUpState.emailError,
                                    usernameError = signUpState.usernameError,
                                    passwordError = signUpState.passwordError,
                                    passwordConfirmError = signUpState.passwordConfirmError,
                                    onEmailChange = signUpActions.onEmailChange,
                                    onUsernameChange = signUpActions.onUsernameChange,
                                    onPasswordChange = signUpActions.onPasswordChange,
                                    onPasswordConfirmChange = signUpActions.onPasswordConfirmChange,
                                    onEmailValidate = signUpActions.onEmailValidate,
                                    onUsernameValidate = signUpActions.onUsernameValidate,
                                    onPasswordValidate = signUpActions.onPasswordValidate,
                                    onPasswordConfirmValidate = signUpActions.onPasswordConfirmValidate,
                                    isFieldsValid = signUpActions.isFieldsValid
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ExpandedLayout(
        currentScreen: String,
        onLoginClick: () -> Unit = {},
        onSignUpClick: () -> Unit = {},
        onBackClick: () -> Unit = {},
        onLoginButtonClick: (String, String) -> Unit = { _, _ -> },
        onSignUpButtonClick: (String, String, String) -> Unit = { _, _, _ -> },
        loginState: LoginState = LoginState(),
        loginActions: LoginActions = LoginActions(),
        signUpState: SignUpState = SignUpState(),
        signUpActions: SignUpActions = SignUpActions(),
        googleButton: @Composable () -> Unit = { CustomGoogleButton() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Primera columna: Título y subtítulo
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(resource = Res.string.app_name),
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimary
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(Res.string.app_subtitle),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                // Segunda columna: Card con contenido
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith ExitTransition.None
                            },
                            modifier = Modifier.weight(1f)
                        ) { screen ->
                            when (screen) {
                                "main" -> MainAuthScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    onLoginClick = onLoginClick,
                                    onSignUpClick = onSignUpClick,
                                    googleButton = googleButton
                                )

                                "login" -> Login(
                                    modifier = Modifier.fillMaxSize(),
                                    onLoginButtonClick = onLoginButtonClick,
                                    onBackClick = onBackClick,
                                    email = loginState.email,
                                    password = loginState.password,
                                    emailError = loginState.emailError,
                                    passwordError = loginState.passwordError,
                                    onEmailChange = loginActions.onEmailChange,
                                    onPasswordChange = loginActions.onPasswordChange,
                                    onEmailValidate = loginActions.onEmailValidate,
                                    onPasswordValidate = loginActions.onPasswordValidate,
                                    isFieldsValid = loginActions.isFieldsValid
                                )

                                "signup" -> SignUp(
                                    modifier = Modifier.fillMaxSize(),
                                    onSignUpButtonClick = onSignUpButtonClick,
                                    onBackClick = onBackClick,
                                    email = signUpState.email,
                                    username = signUpState.username,
                                    password = signUpState.password,
                                    passwordConfirm = signUpState.passwordConfirm,
                                    emailError = signUpState.emailError,
                                    usernameError = signUpState.usernameError,
                                    passwordError = signUpState.passwordError,
                                    passwordConfirmError = signUpState.passwordConfirmError,
                                    onEmailChange = signUpActions.onEmailChange,
                                    onUsernameChange = signUpActions.onUsernameChange,
                                    onPasswordChange = signUpActions.onPasswordChange,
                                    onPasswordConfirmChange = signUpActions.onPasswordConfirmChange,
                                    onEmailValidate = signUpActions.onEmailValidate,
                                    onUsernameValidate = signUpActions.onUsernameValidate,
                                    onPasswordValidate = signUpActions.onPasswordValidate,
                                    onPasswordConfirmValidate = signUpActions.onPasswordConfirmValidate,
                                    isFieldsValid = signUpActions.isFieldsValid
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MainAuthScreen(
        modifier: Modifier = Modifier,
        onLoginClick: () -> Unit = {},
        onSignUpClick: () -> Unit = {},
        googleButton: @Composable () -> Unit = {}
    ) {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Image(
                    painterResource(Res.drawable.happy_bunch___party_of_three__1_),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
            item {
                Spacer(Modifier.height(12.dp))
                ModernButton(
                    label = stringResource(Res.string.sign_up),
                    onClick = onSignUpClick
                )
            }
            item {
                Spacer(Modifier.height(12.dp))
                ModernButton(
                    label = stringResource(Res.string.log_in),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onLoginClick
                )
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Text(
                        text = stringResource(Res.string.or),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
                googleButton()
            }
        }
    }

    @Composable
    fun Login(
        modifier: Modifier = Modifier,
        onLoginButtonClick: (String, String) -> Unit = { _, _ -> },
        onBackClick: () -> Unit = {},
        email: String = "",
        password: String = "",
        emailError: String? = null,
        passwordError: String? = null,
        onEmailChange: (String) -> Unit = {},
        onPasswordChange: (String) -> Unit = {},
        onEmailValidate: () -> Unit = {},
        onPasswordValidate: () -> Unit = {},
        isFieldsValid: () -> Boolean = { false }
    ) {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.Center,
        ) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    Text(
                        stringResource(Res.string.log_in),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    TextButton(
                        onClick = onBackClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "← " + stringResource(Res.string.back),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
            item {
                DivideTextField(
                    label = stringResource(Res.string.email_address_text),
                    value = email,
                    error = emailError,
                    imeAction = ImeAction.Next,
                    onValueChange = onEmailChange,
                    capitalizeFirstLetter = false,
                    validate = onEmailValidate
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                DivideTextField(
                    label = stringResource(Res.string.password_text),
                    value = password,
                    error = passwordError,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    onValueChange = onPasswordChange,
                    validate = onPasswordValidate,
                    onAction = {
                        if (isFieldsValid()) onLoginButtonClick(email, password)
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                ModernButton(
                    label = stringResource(Res.string.log_in),
                    onClick = {
                        if (isFieldsValid()) {
                            onLoginButtonClick(email, password)
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun SignUp(
        modifier: Modifier = Modifier,
        onSignUpButtonClick: (String, String, String) -> Unit,
        onBackClick: () -> Unit,
        email: String = "",
        username: String = "",
        password: String = "",
        passwordConfirm: String = "",
        emailError: String? = null,
        usernameError: String? = null,
        passwordError: String? = null,
        passwordConfirmError: String? = null,
        onEmailChange: (String) -> Unit = {},
        onUsernameChange: (String) -> Unit = {},
        onPasswordChange: (String) -> Unit = {},
        onPasswordConfirmChange: (String) -> Unit = {},
        onEmailValidate: () -> Unit = {},
        onUsernameValidate: () -> Unit = {},
        onPasswordValidate: () -> Unit = {},
        onPasswordConfirmValidate: () -> Unit = {},
        isFieldsValid: () -> Boolean = { false }
    ) {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    Text(
                        stringResource(Res.string.sign_up),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    TextButton(
                        onClick = onBackClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "← " + stringResource(Res.string.back),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
            item {
                DivideTextField(
                    label = stringResource(Res.string.email_address_text),
                    value = email,
                    error = emailError,
                    imeAction = ImeAction.Next,
                    onValueChange = onEmailChange,
                    capitalizeFirstLetter = false,
                    validate = onEmailValidate
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                DivideTextField(
                    label = stringResource(Res.string.username),
                    value = username,
                    error = usernameError,
                    imeAction = ImeAction.Next,
                    onValueChange = onUsernameChange,
                    validate = onUsernameValidate
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                DivideTextField(
                    label = stringResource(Res.string.password_text),
                    value = password,
                    error = passwordError,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                    onValueChange = onPasswordChange,
                    validate = onPasswordValidate
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                DivideTextField(
                    label = stringResource(Res.string.confirm_password_text),
                    value = passwordConfirm,
                    error = passwordConfirmError,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    onValueChange = onPasswordConfirmChange,
                    validate = onPasswordConfirmValidate
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                ModernButton(
                    label = stringResource(Res.string.sign_up),
                    onClick = {
                        if (isFieldsValid()) onSignUpButtonClick(
                            email,
                            password,
                            username
                        )
                    }
                )
            }
        }
    }

    @Composable
    fun ModernButton(
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
        containerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
        label: String
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            ),
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    @Composable
    fun CustomGoogleButton(onClick: () -> Unit = {}) {
        OutlinedButton(
            onClick = onClick,
            shape = ShapeDefaults.Medium,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.height(56.dp).fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painterResource(Res.drawable.ic_google),
                    contentDescription = "Google",
                    modifier = Modifier.size(24.dp)
                )
                Text(text = stringResource(Res.string.connect_with_google))
            }
        }
    }
}
