package com.ragl.divide.domain.usecases.user

import com.ragl.divide.domain.repositories.UserRepository

class UpdateUserNameUseCase(
    private val userRepository: UserRepository
) {
    sealed class Result {
        data class Success(val updated: Boolean) : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(newName: String): Result {
        return try {
            val updated = userRepository.updateUserName(newName)
            Result.Success(updated)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 