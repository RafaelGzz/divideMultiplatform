package com.ragl.divide.domain.usecases.event

import com.ragl.divide.data.models.Event
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
class SaveEventUseCase(
    private val groupRepository: GroupRepository,
    private val userStateHolder: UserStateHolder
) {
    sealed class Result {
        data class Success(val event: Event) : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(groupId: String, event: Event): Result {
        return try {
            val savedEvent = groupRepository.saveEvent(groupId, event)
            userStateHolder.saveEvent(groupId, savedEvent)
            Result.Success(savedEvent)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 