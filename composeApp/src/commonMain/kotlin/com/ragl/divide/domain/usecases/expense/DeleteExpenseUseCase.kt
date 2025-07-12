package com.ragl.divide.domain.usecases.expense

import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.ScheduleNotificationService
import com.ragl.divide.domain.stateHolders.UserStateHolder
class DeleteExpenseUseCase(
    private val userRepository: UserRepository,
    private val userStateHolder: UserStateHolder,
    private val scheduleNotificationService: ScheduleNotificationService,
) {
    sealed class Result {
        object Success : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(expenseId: String): Result {
        return try {
            userRepository.deleteExpense(expenseId)
            userStateHolder.removeExpense(expenseId)
            scheduleNotificationService.cancelNotification(expenseId.takeLast(5).toInt())
            Result.Success
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 