package com.ragl.divide.domain.usecases.friend

import com.ragl.divide.domain.repositories.FriendsRepository

class RemoveFriendUseCase(
    private val friendsRepository: FriendsRepository
) {
    sealed class Result {
        data class Success(val removed: Boolean) : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(friendId: String): Result {
        return try {
            val removed = friendsRepository.removeFriend(friendId)
            Result.Success(removed)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 