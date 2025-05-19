package com.ragl.divide.ui.screens.signIn

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import com.ragl.divide.ui.utils.Strings

class LogInViewModel(
    private val strings: Strings
): ScreenModel {
    var email by mutableStateOf("")
        private set
    var emailError by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var passwordError by mutableStateOf("")
        private set

    fun updateEmail(email: String) {
        this.email = email.trim()
    }

    fun updatePassword(password: String) {
        if(!password.contains(' ')) this.password = password
    }
    fun isFieldsValid(): Boolean {
        return validateEmail().and(validatePassword())
    }
    private fun validateEmail(): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return if (email.isBlank()) {
            emailError = strings.getEmailRequired()
            false
        } else if (email.matches(emailPattern.toRegex())) {
            emailError = ""
            true
        } else {
            emailError = strings.getEmailNotValid()
            false
        }
    }

    private fun validatePassword(): Boolean {
        if (password.isNotBlank()) {
            passwordError = ""
            return true
        } else {
            passwordError = strings.getPasswordRequired()
            return false
        }
    }
}