package com.ragl.divide.domain.usecases.eventPayment

import com.ragl.divide.data.models.EventPayment
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
class SaveEventPaymentUseCase(
    private val groupRepository: GroupRepository,
    private val userStateHolder: UserStateHolder
) {
    sealed class Result {
        data class Success(val payment: EventPayment) : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(groupId: String, payment: EventPayment): Result {
        return try {
            val savedPayment = groupRepository.saveEventPayment(groupId, payment)
            userStateHolder.saveEventPayment(groupId, savedPayment)
            Result.Success(savedPayment)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 