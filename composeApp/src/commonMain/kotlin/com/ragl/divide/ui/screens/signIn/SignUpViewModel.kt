package com.ragl.divide.ui.screens.signIn

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SignUpViewModel: ViewModel() {
    var email by mutableStateOf("")
        private set
    var emailError by mutableStateOf("")
        private set
    var username by mutableStateOf("")
        private set
    var usernameError by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var passwordError by mutableStateOf("")
        private set
    var passwordConfirm by mutableStateOf("")
        private set
    var passwordConfirmError by mutableStateOf("")
        private set

    fun updateEmail(email: String) {
        this.email = email.trim()
    }

    fun updateUsername(username: String) {
        this.username = username.trim()
    }

    fun updatePassword(password: String) {
        this.password = password.trim()
    }

    fun updatePasswordConfirm(passwordConfirm: String) {
        this.passwordConfirm = passwordConfirm.trim()
    }

    fun isFieldsValid(): Boolean {
        return validateEmail().and(validateUsername().and(validatePassword().and(validatePasswordConfirm())))
    }

    private fun validateEmail(): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return if (email.isEmpty()) {
            emailError = "Email Address is required"
            false
        } else if (!email.matches(emailPattern.toRegex())) {
            emailError = "Invalid email address"
            false
        } else {
            emailError = ""
            true
        }
    }

    private fun validateUsername(): Boolean {
        val usernamePattern = "(?!.*[.]{2,})^[a-zA-Z0-9.\\-_]{3,20}\$"

        return if (username.isEmpty()) {
            usernameError = "Username cannot be empty"
            false
        } else if (username.matches(usernamePattern.toRegex())) {
            usernameError = ""
            true
        } else {
            usernameError =
                "Username must be between 3 and 20 characters and can only contain letters, numbers, underscores, and hyphens"
            false
        }
    }

    private fun validatePassword(): Boolean {
        val passwordPattern = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}\$"
        if (password.isBlank()) {
            passwordError = "Password is required"
            return false
        }

        if (password.length < 8) {
            passwordError = "Password must be at least 8 characters"
        }

        if (!password.matches(passwordPattern.toRegex())) {
            passwordError =
                "Password must contain at least one number, one uppercase letter, one lowercase letter and one special character"
            return false
        }
        passwordError = ""
        return true
    }

    private fun validatePasswordConfirm(): Boolean {
        passwordConfirmError = if (passwordConfirm.isNotBlank() && passwordConfirm == password) {
            ""
        } else {
            "Passwords do not match"
        }
        return passwordConfirmError.isEmpty()
    }
}