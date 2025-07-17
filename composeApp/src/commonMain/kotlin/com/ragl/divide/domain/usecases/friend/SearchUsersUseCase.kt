package com.ragl.divide.domain.usecases.friend

import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.repositories.FriendsRepository
class SearchUsersUseCase(
    private val friendsRepository: FriendsRepository
) {
    sealed class Result {
        data class Success(val users: Map<String, UserInfo>) : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(query: String, existing: List<UserInfo>): Result {
        return try {
            val users = friendsRepository.searchUsers(query, existing)
            Result.Success(users)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 