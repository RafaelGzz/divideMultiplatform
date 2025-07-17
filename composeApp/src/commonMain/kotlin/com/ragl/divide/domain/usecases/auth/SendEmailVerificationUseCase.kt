package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.domain.repositories.UserRepository

class SendEmailVerificationUseCase(
    private val userRepository: UserRepository,
) {
    sealed class Result {
        object Success : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(): Result {
        return try {
            userRepository.sendEmailVerification()
            Result.Success
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 