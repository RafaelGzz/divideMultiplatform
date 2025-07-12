package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.domain.repositories.UserRepository
class CheckEmailVerificationUseCase(
    private val userRepository: UserRepository,
) {
    sealed class Result {
        data class Success(val isVerified: Boolean) : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(): Result {
        return try {
            val isVerified = userRepository.isEmailVerified()
            Result.Success(isVerified)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 