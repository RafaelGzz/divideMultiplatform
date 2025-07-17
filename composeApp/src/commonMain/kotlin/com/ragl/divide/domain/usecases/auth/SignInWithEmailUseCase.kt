package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.data.models.User
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.AnalyticsService

class SignInWithEmailUseCase(
    private val userRepository: UserRepository,
    private val analyticsService: AnalyticsService,
) {
    sealed class Result {
        data class Success(val user: User) : Result()
        data class Error(val exception: Exception) : Result()
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
                Result.Error(Exception("Failed to login"))
            }
        } catch (e: Exception) {
            analyticsService.logError(e, "Error en login con email")
            Result.Error(e)
        }
    }


} 