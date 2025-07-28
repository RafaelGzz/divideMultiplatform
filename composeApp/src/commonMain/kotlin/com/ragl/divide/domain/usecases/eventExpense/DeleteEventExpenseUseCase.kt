package com.ragl.divide.domain.usecases.eventExpense

import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.utils.logMessage

class DeleteEventExpenseUseCase(
    private val groupRepository: GroupRepository,
    private val userStateHolder: UserStateHolder,
) {
    sealed class Result {
        object Success : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(groupId: String, expense: EventExpense): Result {
        return try {
            groupRepository.deleteEventExpense(groupId, expense)
            userStateHolder.deleteEventExpense(groupId, expense)
            Result.Success
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 