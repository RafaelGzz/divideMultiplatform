package com.ragl.divide.domain.usecases.payment

import com.ragl.divide.data.models.Expense
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder

class SaveExpensePaymentUseCase(
    private val userRepository: UserRepository,
    private val userStateHolder: UserStateHolder
) {
    sealed class Result {
        data object Success : Result()
        data object Paid : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(expense: Expense): Result {
        return try {
            userRepository.saveExpense(expense)
            userStateHolder.saveExpense(expense)
            if (expense.paid) {
                Result.Paid
            } else {
                Result.Success
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 