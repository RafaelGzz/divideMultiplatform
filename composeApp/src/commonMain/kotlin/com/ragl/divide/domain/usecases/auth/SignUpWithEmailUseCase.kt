package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.AnalyticsService

class SignUpWithEmailUseCase(
    private val userRepository: UserRepository,
    private val analyticsService: AnalyticsService,
) {
    sealed class Result {
        object Success : Result()
        data class Error(val exception: Exception) : Result()
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
                Result.Error(Exception("Failed to sign up"))
            }
        } catch (e: Exception) {
            analyticsService.logError(e, "Error en registro con email")
            Result.Error(e)
        }
    }


} 