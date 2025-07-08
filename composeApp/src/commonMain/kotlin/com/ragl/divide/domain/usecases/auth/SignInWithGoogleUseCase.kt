package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.data.models.User
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.AnalyticsService
import com.ragl.divide.presentation.utils.Strings
import dev.gitlive.firebase.auth.FirebaseUser

class SignInWithGoogleUseCase(
    private val userRepository: UserRepository,
    private val analyticsService: AnalyticsService,
    private val strings: Strings
) {
    sealed class Result {
        data class Success(val user: User, val isNewUser: Boolean) : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(firebaseUserResult: kotlin.Result<FirebaseUser?>): Result {
        return try {
            val firebaseUser = firebaseUserResult.getOrNull()
            if (firebaseUser != null) {
                val existingUser = userRepository.getUser(firebaseUser.uid)
                val isNewUser = existingUser.uuid.isEmpty()
                
                val user = if (isNewUser) {
                    val newUser = userRepository.createUserInDatabase()
                    analyticsService.logEvent("sign_up", mapOf(
                        "method" to "google",
                        "email" to newUser.email
                    ))
                    newUser
                } else {
                    analyticsService.logEvent("login", mapOf(
                        "method" to "google",
                        "email" to existingUser.email
                    ))
                    existingUser
                }
                
                Result.Success(user, isNewUser)
            } else {
                val exception = firebaseUserResult.exceptionOrNull()
                analyticsService.logError(exception ?: Exception("Unknown Google sign in error"), "Error en login con google")
                Result.Error(strings.getFailedToLogin())
            }
        } catch (e: Exception) {
            analyticsService.logError(e, "Error en login con google")
            Result.Error(e.message ?: strings.getUnknownError())
        }
    }
} 