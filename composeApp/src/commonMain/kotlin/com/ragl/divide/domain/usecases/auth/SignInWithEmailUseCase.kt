package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.data.models.User
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.AnalyticsService
import com.ragl.divide.presentation.utils.Strings

class SignInWithEmailUseCase(
    private val userRepository: UserRepository,
    private val analyticsService: AnalyticsService,
    private val strings: Strings
) {
    sealed class Result {
        data class Success(val user: User) : Result()
        data class Error(val message: String) : Result()
        object EmailNotVerified : Result()
    }

    suspend operator fun invoke(email: String, password: String): Result {
        return try {
            val user = userRepository.signInWithEmailAndPassword(email, password)
            if (user != null) {
                if (userRepository.isEmailVerified()) {
                    analyticsService.logEvent("login", mapOf("method" to "email"))
                    Result.Success(user)
                } else {
                    Result.EmailNotVerified
                }
            } else {
                Result.Error(strings.getFailedToLogin())
            }
        } catch (e: Exception) {
            analyticsService.logError(e, "Error en login con email")
            Result.Error(handleAuthError(e))
        }
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
} 