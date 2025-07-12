package com.ragl.divide.domain.usecases.group

import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
class DeleteGroupUseCase(
    private val groupRepository: GroupRepository,
    private val userStateHolder: UserStateHolder,
) {
    sealed class Result {
        object Success : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(groupId: String, image: String): Result {
        return try {
            groupRepository.deleteGroup(groupId, image)
            userStateHolder.deleteGroup(groupId)
            Result.Success
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 