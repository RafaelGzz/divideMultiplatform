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
): ScreenModel {
    
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
        return validateSignUpEmail().and(validateSignUpUsername().and(validateSignUpPassword().and(validateSignUpPasswordConfirm())))
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
        signUpPasswordConfirmError = if (signUpPasswordConfirm.isNotBlank() && signUpPasswordConfirm == signUpPassword) {
            ""
        } else {
            strings.getPasswordsNotMatch()
        }
        return signUpPasswordConfirmError.isEmpty()
    }

    fun handleError(message: String?) {
        appStateService.handleError(message ?: strings.getUnknownError())
    }

    fun handleSuccess(message: String) {
        appStateService.handleSuccess(message)
    }

    private fun showLoading() {
        appStateService.showLoading()
    }

    private fun hideLoading() {
        appStateService.hideLoading()
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
            try {
                showLoading()
                when (val result = signInWithEmailUseCase(email, password)) {
                    is SignInWithEmailUseCase.Result.Success -> {
                        onSuccess()
                    }
                    is SignInWithEmailUseCase.Result.EmailNotVerified -> {
                        onFail(strings.getEmailNotVerified())
                    }
                    is SignInWithEmailUseCase.Result.Error -> {
                        onFail(result.message)
                    }
                }
            } catch (e: Exception) {
                onFail(e.message ?: strings.getUnknownError())
                logMessage("AuthViewModel: signInWithEmailAndPassword", e.message.toString())
            } finally {
                hideLoading()
            }
        }
    }

    fun signUpWithEmailAndPassword(
        email: String, password: String, name: String
    ) {
        screenModelScope.launch {
            try {
                showLoading()
                when (val result = signUpWithEmailUseCase(email, password, name)) {
                    is SignUpWithEmailUseCase.Result.Success -> {
                        handleSuccess(strings.getVerificationEmailSent())
                    }
                    is SignUpWithEmailUseCase.Result.Error -> {
                        handleError(result.message)
                    }
                }
            } catch (e: Exception) {
                handleError(e.message ?: strings.getUnknownError())
                logMessage("AuthViewModel: signUpWithEmailAndPassword", e.message.toString())
            } finally {
                hideLoading()
            }
        }
    }

    fun signInWithGoogle(
        result: Result<FirebaseUser?>,
        onSuccess: () -> Unit
    ) {
        screenModelScope.launch {
            try {
                showLoading()
                when (val useCaseResult = signInWithGoogleUseCase(result)) {
                    is SignInWithGoogleUseCase.Result.Success -> {
                        onSuccess()
                    }
                    is SignInWithGoogleUseCase.Result.Error -> {
                        handleError(useCaseResult.message)
                        logMessage("AuthViewModel: signInWithGoogle", useCaseResult.message)
                    }
                }
            } catch (e: Exception) {
                handleError(e.message ?: strings.getUnknownError())
                logMessage("AuthViewModel", "${e.message}")
            } finally {
                hideLoading()
            }
        }
    }

    fun signOut(onSignOut: () -> Unit) {
        screenModelScope.launch {
            try {
                showLoading()
                when (val result = signOutUseCase()) {
                    is SignOutUseCase.Result.Success -> {
                        onSignOut()
                    }
                    is SignOutUseCase.Result.Error -> {
                        handleError(result.message)
                        logMessage("AuthViewModel", result.message)
                    }
                }
            } catch (e: Exception) {
                handleError(e.message ?: strings.getUnknownError())
                logMessage("AuthViewModel", e.message.toString())
            } finally {
                hideLoading()
            }
        }
    }

    fun resendVerificationEmail() {
        screenModelScope.launch {
            try {
                showLoading()
                when (val result = sendEmailVerificationUseCase()) {
                    is SendEmailVerificationUseCase.Result.Success -> {
                        handleSuccess(strings.getVerificationEmailSent())
                        startCountdown()
                    }
                    is SendEmailVerificationUseCase.Result.Error -> {
                        handleError(result.message)
                    }
                }
            } catch (e: Exception) {
                handleError(e.message ?: strings.getUnknownError())
            } finally {
                hideLoading()
            }
        }
    }

    suspend fun isEmailVerified(): Boolean {
        var res = false
        try {
            showLoading()
            when (val result = checkEmailVerificationUseCase()) {
                is CheckEmailVerificationUseCase.Result.Success -> {
                    res = result.isVerified
                }
                is CheckEmailVerificationUseCase.Result.Error -> {
                    handleError(result.message)
                }
            }
        } catch (e: Exception) {
            handleError(e.message ?: strings.getUnknownError())
        } finally {
            hideLoading()
        }
        return res
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
} 