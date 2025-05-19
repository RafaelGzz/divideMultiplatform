package com.ragl.divide.ui.screens.signIn

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import com.ragl.divide.ui.utils.Strings

class SignUpViewModel(
    private val strings: Strings
): ScreenModel {
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
            emailError = strings.getEmailAddressRequired()
            false
        } else if (!email.matches(emailPattern.toRegex())) {
            emailError = strings.getInvalidEmailAddress()
            false
        } else {
            emailError = ""
            true
        }
    }

    private fun validateUsername(): Boolean {
        val usernamePattern = "(?!.*[.]{2,})^[a-zA-Z0-9.\\-_]{3,20}\$"

        return if (username.isEmpty()) {
            usernameError = strings.getUsernameEmpty()
            false
        } else if (username.matches(usernamePattern.toRegex())) {
            usernameError = ""
            true
        } else {
            usernameError = strings.getUsernameRequirements()
            false
        }
    }

    private fun validatePassword(): Boolean {
        val passwordPattern = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z\\d]).{8,}\$"
        if (password.isBlank()) {
            passwordError = strings.getPasswordRequired()
            return false
        }

        if (password.length < 8) {
            passwordError = strings.getPasswordMinLength()
            return false
        }

        if (!password.matches(passwordPattern.toRegex())) {
            passwordError = strings.getPasswordRequirements()
            return false
        }
        passwordError = ""
        return true
    }

    private fun validatePasswordConfirm(): Boolean {
        passwordConfirmError = if (passwordConfirm.isNotBlank() && passwordConfirm == password) {
            ""
        } else {
            strings.getPasswordsNotMatch()
        }
        return passwordConfirmError.isEmpty()
    }
}