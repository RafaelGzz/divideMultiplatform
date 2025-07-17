package com.ragl.divide.domain.usecases.expense

import com.ragl.divide.data.models.Expense
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.ScheduleNotificationService
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.utils.Strings
class SaveExpenseUseCase(
    private val userRepository: UserRepository,
    private val userStateHolder: UserStateHolder,
    private val scheduleNotificationService: ScheduleNotificationService,
    private val strings: Strings
) {
    sealed class Result {
        data object Success : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(expense: Expense): Result {
        return try {
            val savedExpense = userRepository.saveExpense(expense)
            userStateHolder.saveExpense(savedExpense)
            scheduleNotificationService.cancelNotification(
                savedExpense.id.takeLast(5).toInt()
            )
            if (savedExpense.reminders) {
                scheduleNotificationService.scheduleNotification(
                    id = savedExpense.id.takeLast(5).toInt(),
                    title = strings.getAppName(),
                    message = strings.getNotificationBodyString(savedExpense.title),
                    startingDateMillis = savedExpense.startingDate,
                    frequency = savedExpense.frequency,
                    true
                )
            }
            Result.Success
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 