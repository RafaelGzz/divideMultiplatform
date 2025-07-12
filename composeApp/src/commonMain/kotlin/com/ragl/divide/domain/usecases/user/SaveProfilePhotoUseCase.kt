package com.ragl.divide.domain.usecases.user

import com.ragl.divide.domain.repositories.UserRepository
import dev.gitlive.firebase.storage.File

class SaveProfilePhotoUseCase(
    private val userRepository: UserRepository
) {
    sealed class Result {
        data class Success(val photoUrl: String) : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(photo: File): Result {
        return try {
            val photoUrl = userRepository.saveProfilePhoto(photo)
            Result.Success(photoUrl)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 