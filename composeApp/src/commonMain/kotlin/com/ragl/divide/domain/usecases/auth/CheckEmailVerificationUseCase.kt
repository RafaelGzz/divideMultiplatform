package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.presentation.utils.Strings

class CheckEmailVerificationUseCase(
    private val userRepository: UserRepository,
    private val strings: Strings
) {
    sealed class Result {
        data class Success(val isVerified: Boolean) : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(): Result {
        return try {
            val isVerified = userRepository.isEmailVerified()
            Result.Success(isVerified)
        } catch (e: Exception) {
            Result.Error(e.message ?: strings.getSomethingWentWrong())
        }
    }
} 