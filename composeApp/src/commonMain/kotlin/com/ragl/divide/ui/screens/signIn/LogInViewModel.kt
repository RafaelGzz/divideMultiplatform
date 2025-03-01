package com.ragl.divide.ui.screens.signIn

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LogInViewModel: ViewModel() {
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
        return true
    //return validateEmail().and(validatePassword())
    }
    private fun validateEmail(): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return if (email.isBlank()) {
            emailError = "Email is required"
            false
        } else if (email.matches(emailPattern.toRegex())) {
            emailError = ""
            true
        } else {
            emailError = "Email is not valid"
            false
        }
    }

    private fun validatePassword(): Boolean {
        if (password.isNotBlank()) {
            passwordError = ""
            return true
        } else {
            passwordError = "Password is required"
            return false
        }
    }
}