package com.ragl.divide.domain.usecases.friend

import com.ragl.divide.domain.repositories.FriendsRepository

class AcceptFriendRequestUseCase(
    private val friendsRepository: FriendsRepository
) {
    sealed class Result {
        data class Success(val accepted: Boolean) : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(friendId: String): Result {
        return try {
            val accepted = friendsRepository.acceptFriendRequest(friendId)
            Result.Success(accepted)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 