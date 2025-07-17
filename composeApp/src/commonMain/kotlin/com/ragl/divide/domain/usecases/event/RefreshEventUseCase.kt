package com.ragl.divide.domain.usecases.event

import com.ragl.divide.data.models.Event
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
class RefreshEventUseCase(
    private val groupRepository: GroupRepository,
    private val userStateHolder: UserStateHolder,
) {
    sealed class Result {
        data class Success(val event: Event) : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(groupId: String, eventId: String): Result {
        return try {
            val event = groupRepository.getEvent(groupId, eventId)
            userStateHolder.updateEventInState(groupId, eventId, event)
            Result.Success(event)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 