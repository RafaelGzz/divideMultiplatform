package com.ragl.divide.domain.usecases.friend

import com.ragl.divide.domain.repositories.FriendsRepository

class SendFriendRequestUseCase(
    private val friendsRepository: FriendsRepository
) {
    sealed class Result {
        data class Success(val sent: Boolean) : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(friendId: String): Result {
        return try {
            val sent = friendsRepository.sendFriendRequest(friendId)
            Result.Success(sent)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 