package com.ragl.divide.domain.usecases.eventPayment

import com.ragl.divide.data.models.EventPayment
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.utils.logMessage

class DeleteEventPaymentUseCase(
    private val groupRepository: GroupRepository,
    private val userStateHolder: UserStateHolder,
) {
    sealed class Result {
        object Success : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(groupId: String, payment: EventPayment): Result {
        return try {
            groupRepository.deleteEventPayment(groupId, payment)
            userStateHolder.deleteEventPayment(groupId, payment)
            Result.Success
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 