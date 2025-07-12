package com.ragl.divide.presentation.screens.signIn

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.domain.services.AppStateService
import com.ragl.divide.domain.usecases.auth.CheckEmailVerificationUseCase
import com.ragl.divide.domain.usecases.auth.SendEmailVerificationUseCase
import com.ragl.divide.domain.usecases.auth.SignInWithEmailUseCase
import com.ragl.divide.domain.usecases.auth.SignInWithGoogleUseCase
import com.ragl.divide.domain.usecases.auth.SignOutUseCase
import com.ragl.divide.domain.usecases.auth.SignUpWithEmailUseCase
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.logMessage
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val strings: Strings,
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    private val checkEmailVerificationUseCase: CheckEmailVerificationUseCase,
    private val appStateService: AppStateService
) : ScreenModel {

    private val _countdown = MutableStateFlow(0)
    val countdown = _countdown.asStateFlow()

    var loginEmail by mutableStateOf("")
        private set
    var loginEmailError by mutableStateOf("")
        private set
    var loginPassword by mutableStateOf("")
        private set
    var loginPasswordError by mutableStateOf("")
        private set

    var signUpEmail by mutableStateOf("")
        private set
    var signUpEmailError by mutableStateOf("")
        private set
    var signUpUsername by mutableStateOf("")
        private set
    var signUpUsernameError by mutableStateOf("")
        private set
    var signUpPassword by mutableStateOf("")
        private set
    var signUpPasswordError by mutableStateOf("")
        private set
    var signUpPasswordConfirm by mutableStateOf("")
        private set
    var signUpPasswordConfirmError by mutableStateOf("")
        private set

    fun updateLoginEmail(email: String) {
        this.loginEmail = email.trim()
    }

    fun updateLoginPassword(password: String) {
        if (!password.contains(' ')) this.loginPassword = password
    }

    fun isLoginFieldsValid(): Boolean {
        return validateLoginEmail().and(validateLoginPassword())
    }

    fun validateLoginEmail(): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return if (loginEmail.isBlank()) {
            loginEmailError = strings.getEmailRequired()
            false
        } else if (loginEmail.matches(emailPattern.toRegex())) {
            loginEmailError = ""
            true
        } else {
            loginEmailError = strings.getEmailNotValid()
            false
        }
    }

    fun validateLoginPassword(): Boolean {
        if (loginPassword.isNotBlank()) {
            loginPasswordError = ""
            return true
        } else {
            loginPasswordError = strings.getPasswordRequired()
            return false
        }
    }

    fun updateSignUpEmail(email: String) {
        this.signUpEmail = email.trim()
    }

    fun updateSignUpUsername(username: String) {
        this.signUpUsername = username.trim()
    }

    fun updateSignUpPassword(password: String) {
        this.signUpPassword = password.trim()
    }

    fun updateSignUpPasswordConfirm(passwordConfirm: String) {
        this.signUpPasswordConfirm = passwordConfirm.trim()
    }

    fun isSignUpFieldsValid(): Boolean {
        return validateSignUpEmail().and(
            validateSignUpUsername().and(
                validateSignUpPassword().and(
                    validateSignUpPasswordConfirm()
                )
            )
        )
    }

    fun validateSignUpEmail(): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return if (signUpEmail.isEmpty()) {
            signUpEmailError = strings.getEmailAddressRequired()
            false
        } else if (!signUpEmail.matches(emailPattern.toRegex())) {
            signUpEmailError = strings.getInvalidEmailAddress()
            false
        } else {
            signUpEmailError = ""
            true
        }
    }

    fun validateSignUpUsername(): Boolean {
        val usernamePattern = "(?!.*[.]{2,})^[a-zA-Z0-9.\\-_]{3,20}\$"

        return if (signUpUsername.isEmpty()) {
            signUpUsernameError = strings.getUsernameEmpty()
            false
        } else if (signUpUsername.matches(usernamePattern.toRegex())) {
            signUpUsernameError = ""
            true
        } else {
            signUpUsernameError = strings.getUsernameRequirements()
            false
        }
    }

    fun validateSignUpPassword(): Boolean {
        val passwordPattern = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z\\d]).{8,}\$"
        if (signUpPassword.isBlank()) {
            signUpPasswordError = strings.getPasswordRequired()
            return false
        }

        if (signUpPassword.length < 8) {
            signUpPasswordError = strings.getPasswordMinLength()
            return false
        }

        if (!signUpPassword.matches(passwordPattern.toRegex())) {
            signUpPasswordError = strings.getPasswordRequirements()
            return false
        }
        signUpPasswordError = ""
        return true
    }

    fun validateSignUpPasswordConfirm(): Boolean {
        signUpPasswordConfirmError =
            if (signUpPasswordConfirm.isNotBlank() && signUpPasswordConfirm == signUpPassword) {
                ""
            } else {
                strings.getPasswordsNotMatch()
            }
        return signUpPasswordConfirmError.isEmpty()
    }

    fun handleError(message: String?) {
        appStateService.handleError(message ?: strings.getUnknownError())
    }

    fun resetLoginFields() {
        loginEmail = ""
        loginEmailError = ""
        loginPassword = ""
        loginPasswordError = ""
    }

    fun resetSignUpFields() {
        signUpEmail = ""
        signUpEmailError = ""
        signUpUsername = ""
        signUpUsernameError = ""
        signUpPassword = ""
        signUpPasswordError = ""
        signUpPasswordConfirm = ""
        signUpPasswordConfirmError = ""
    }

    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFail: (String) -> Unit
    ) {
        screenModelScope.launch {
            appStateService.showLoading()
            when (val result = signInWithEmailUseCase(email, password)) {
                is SignInWithEmailUseCase.Result.Success -> {
                    onSuccess()
                }

                is SignInWithEmailUseCase.Result.EmailNotVerified -> {
                    onFail(strings.getEmailNotVerified())
                }

                is SignInWithEmailUseCase.Result.Error -> {
                    logMessage("SignInWithEmailUseCase", result.exception.message ?: result.exception.stackTraceToString())
                    onFail(handleSignInError(result.exception))
                }
            }
            appStateService.hideLoading()
        }
    }

    fun signUpWithEmailAndPassword(
        email: String, password: String, name: String
    ) {
        screenModelScope.launch {
            appStateService.showLoading()
            when (val result = signUpWithEmailUseCase(email, password, name)) {
                is SignUpWithEmailUseCase.Result.Success -> {
                    appStateService.handleSuccess(strings.getVerificationEmailSent())
                }

                is SignUpWithEmailUseCase.Result.Error -> {
                    logMessage("SignUpWithEmailUseCase", result.exception.message ?: result.exception.stackTraceToString())
                    appStateService.handleError(handleSignUpError(result.exception))
                }
            }
            appStateService.hideLoading()
        }
    }

    fun signInWithGoogle(
        result: Result<FirebaseUser?>,
        onSuccess: () -> Unit
    ) {
        screenModelScope.launch {
            appStateService.showLoading()
            when (val useCaseResult = signInWithGoogleUseCase(result)) {
                is SignInWithGoogleUseCase.Result.Success -> {
                    onSuccess()
                }

                is SignInWithGoogleUseCase.Result.Error -> {
                    logMessage("SignInWithGoogleUseCase", useCaseResult.exception.message ?: useCaseResult.exception.stackTraceToString())
                    appStateService.handleError(strings.getUnknownError())
                }
            }
            appStateService.hideLoading()
        }
    }

    fun signOut(onSignOut: () -> Unit) {
        screenModelScope.launch {
            appStateService.showLoading()
            when (val result = signOutUseCase()) {
                is SignOutUseCase.Result.Success -> {
                    onSignOut()
                }

                is SignOutUseCase.Result.Error -> {
                    logMessage("SignOutUseCase", result.exception.message ?: result.exception.stackTraceToString())
                    appStateService.handleError(strings.getUnknownError())
                }
            }
            appStateService.hideLoading()
        }
    }

    fun resendVerificationEmail() {
        screenModelScope.launch {
            appStateService.showLoading()
            when (val result = sendEmailVerificationUseCase()) {
                is SendEmailVerificationUseCase.Result.Success -> {
                    appStateService.handleSuccess(strings.getVerificationEmailSent())
                    startCountdown()
                }

                is SendEmailVerificationUseCase.Result.Error -> {
                    logMessage("SendEmailVerificationUseCase", result.exception.message ?: result.exception.stackTraceToString())
                    appStateService.handleError(strings.getUnknownError())
                }
            }
            appStateService.hideLoading()
        }
    }

    suspend fun isEmailVerified(onSuccess: () -> Unit) {
        appStateService.showLoading()
        when (val result = checkEmailVerificationUseCase()) {
            is CheckEmailVerificationUseCase.Result.Success -> {
                if (result.isVerified) {
                    onSuccess()
                } else {
                    appStateService.handleError(strings.getEmailNotVerified())
                }
            }

            is CheckEmailVerificationUseCase.Result.Error -> {
                logMessage("CheckEmailVerificationUseCase", result.exception.message ?: result.exception.stackTraceToString())
                appStateService.handleError(strings.getUnknownError())
            }
        }
        appStateService.hideLoading()
    }

    private fun startCountdown() {
        screenModelScope.launch {
            _countdown.value = 300
            while (_countdown.value > 0) {
                delay(1000)
                _countdown.value--
            }
        }
    }

    private fun handleSignInError(e: Exception): String {
        return when {
            e.message?.contains("no user record") == true ||
                    e.message?.contains("password is invalid") == true -> {
                strings.getEmailPasswordInvalid()
            }
            e.message?.contains("email address is already in use") == true -> {
                strings.getEmailAlreadyInUse()
            }
            e.message?.contains("unusual activity") == true -> {
                strings.getUnusualActivity()
            }
            else -> {
                e.message ?: strings.getUnknownError()
            }
        }
    }

    private fun handleSignUpError(e: Exception): String {
        return when {
            e.message?.contains("email address is already in use") == true -> {
                strings.getEmailAlreadyInUse()
            }
            e.message?.contains("weak-password") == true -> {
                strings.getPasswordMinLength()
            }
            e.message?.contains("invalid-email") == true -> {
                strings.getInvalidEmailAddress()
            }
            else -> {
                e.message ?: strings.getUnknownError()
            }
        }
    }
} 