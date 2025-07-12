package com.ragl.divide.domain.usecases.eventExpense

import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.logMessage

class DeleteEventExpenseUseCase(
    private val groupRepository: GroupRepository,
    private val userStateHolder: UserStateHolder,
    private val strings: Strings
) {
    sealed class Result {
        object Success : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(groupId: String, expense: EventExpense): Result {
        return try {
            groupRepository.deleteEventExpense(groupId, expense)
            userStateHolder.deleteEventExpense(groupId, expense)
            Result.Success
        } catch (e: Exception) {
            logMessage("DeleteEventExpenseUseCase", e.message ?: e.stackTraceToString())
            Result.Error(strings.getUnknownError())
        }
    }
} 