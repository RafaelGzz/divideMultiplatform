package com.ragl.divide.domain.usecases.eventExpense

import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.utils.logMessage

class SaveEventExpenseUseCase(
    private val groupRepository: GroupRepository,
    private val userStateHolder: UserStateHolder,
) {
    sealed class Result {
        data class Success(val expense: EventExpense) : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(groupId: String, expense: EventExpense): Result {
        return try {
            val savedExpense = groupRepository.saveEventExpense(groupId, expense)
            userStateHolder.saveEventExpense(groupId, savedExpense)
            Result.Success(savedExpense)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 