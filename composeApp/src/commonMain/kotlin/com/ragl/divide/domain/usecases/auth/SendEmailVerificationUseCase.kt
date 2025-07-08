package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.presentation.utils.Strings

class SendEmailVerificationUseCase(
    private val userRepository: UserRepository,
    private val strings: Strings
) {
    sealed class Result {
        object Success : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(): Result {
        return try {
            userRepository.sendEmailVerification()
            Result.Success
        } catch (e: Exception) {
            Result.Error(e.message ?: strings.getSomethingWentWrong())
        }
    }
} 