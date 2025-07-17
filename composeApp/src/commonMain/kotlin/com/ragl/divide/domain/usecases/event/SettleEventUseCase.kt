package com.ragl.divide.domain.usecases.event

import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
class SettleEventUseCase(
    private val groupRepository: GroupRepository,
    private val userStateHolder: UserStateHolder
) {
    sealed class Result {
        object Success : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(groupId: String, eventId: String): Result {
        return try {
            groupRepository.settleEvent(groupId, eventId)
            userStateHolder.settleEvent(groupId, eventId)
            Result.Success
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 