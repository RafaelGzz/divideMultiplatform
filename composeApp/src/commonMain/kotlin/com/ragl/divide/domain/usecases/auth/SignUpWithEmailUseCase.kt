package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.AnalyticsService
import com.ragl.divide.presentation.utils.Strings

class SignUpWithEmailUseCase(
    private val userRepository: UserRepository,
    private val analyticsService: AnalyticsService,
    private val strings: Strings
) {
    sealed class Result {
        object Success : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(email: String, password: String, name: String): Result {
        return try {
            val user = userRepository.signUpWithEmailAndPassword(email, password, name)
            if (user != null) {
                analyticsService.logEvent("sign_up", mapOf(
                    "method" to "email",
                    "email" to email
                ))
                userRepository.signOut()
                Result.Success
            } else {
                Result.Error(strings.getSomethingWentWrong())
            }
        } catch (e: Exception) {
            analyticsService.logError(e, "Error en registro con email")
            Result.Error(handleAuthError(e))
        }
    }

    private fun handleAuthError(e: Exception): String {
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