package com.ragl.divide.ui.screens.signIn

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.repositories.UserRepository
import com.ragl.divide.data.services.AnalyticsService
import com.ragl.divide.data.services.AppStateService
import com.ragl.divide.data.services.ScheduleNotificationService
import com.ragl.divide.ui.utils.Strings
import com.ragl.divide.ui.utils.logMessage
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val strings: Strings,
    private val userRepository: UserRepository,
    private val analyticsService: AnalyticsService,
    private val scheduleNotificationService: ScheduleNotificationService,
    private val appStateService: AppStateService
): ScreenModel {
    
    // Only countdown state is specific to auth
    private val _countdown = MutableStateFlow(0)
    val countdown = _countdown.asStateFlow()

    // Login fields
    var loginEmail by mutableStateOf("")
        private set
    var loginEmailError by mutableStateOf("")
        private set
    var loginPassword by mutableStateOf("")
        private set
    var loginPasswordError by mutableStateOf("")
        private set

    // SignUp fields
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

    // Login methods
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

    // SignUp methods
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

    // Loading, error and success handling methods
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
                if (userRepository.signInWithEmailAndPassword(email, password) != null) {
                    if (userRepository.isEmailVerified()) {
                        analyticsService.logEvent("login", mapOf(
                            "method" to "email"
                        ))
                        onSuccess()
                    } else {
                        onFail(strings.getEmailNotVerified())
                    }
                } else onFail(strings.getFailedToLogin())
            } catch (e: Exception) {
                analyticsService.logError(e, "Error en login con email")
                onFail(handleAuthError(e))
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
                if (userRepository.signUpWithEmailAndPassword(email, password, name) != null) {
                    analyticsService.logEvent("sign_up", mapOf(
                        "method" to "email",
                        "email" to email
                    ))
                    userRepository.signOut()
                    handleSuccess(strings.getVerificationEmailSent())
                } else handleError(strings.getFailedToLogin())
            } catch (e: Exception) {
                analyticsService.logError(e, "Error en registro con email")
                handleError(handleAuthError(e))
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
                val firebaseUser = result.getOrNull()
                if (firebaseUser != null) {
                    val checkedUser = userRepository.getUser(firebaseUser.uid)
                    if (checkedUser.uuid.isEmpty()) {
                        val newUser = userRepository.createUserInDatabase()
                        analyticsService.logEvent("sign_up", mapOf(
                            "method" to "google",
                            "email" to newUser.email
                        ))
                    } else {
                        analyticsService.logEvent("login", mapOf(
                            "method" to "google",
                            "email" to checkedUser.email
                        ))
                    }
                    onSuccess()
                } else {
                    handleError(strings.getFailedToLogin())
                    analyticsService.logError(result.exceptionOrNull()!!, "Error en login con google")
                    logMessage(
                        "AuthViewModel: signInWithGoogle",
                        "${result.exceptionOrNull()?.message}"
                    )
                }

            } catch (e: Exception) {
                analyticsService.logError(e, "Error en login con google")
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
                scheduleNotificationService.cancelAllNotifications()
                userRepository.signOut()
                if (userRepository.getFirebaseUser() == null) {
                    onSignOut()
                }
            } catch (e: Exception) {
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
                userRepository.sendEmailVerification()
                handleSuccess(strings.getVerificationEmailSent())
                startCountdown()
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
            res = userRepository.isEmailVerified()
        } catch (e: Exception) {
            handleError(e.message ?: strings.getUnknownError())
        } finally {
            hideLoading()
        }
        return res
    }

    private fun handleAuthError(e: Exception): String {
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