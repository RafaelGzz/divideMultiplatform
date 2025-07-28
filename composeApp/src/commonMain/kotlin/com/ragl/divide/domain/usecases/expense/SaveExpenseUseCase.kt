package com.ragl.divide.domain.usecases.expense

import com.ragl.divide.data.models.Expense
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder

class SaveExpenseUseCase(
    private val userRepository: UserRepository,
    private val userStateHolder: UserStateHolder
) {
    sealed class Result {
        data class Success(val expense: Expense) : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(expense: Expense): Result {
        return try {
            val savedExpense = userRepository.saveExpense(expense)
            userStateHolder.saveExpense(savedExpense)
            Result.Success(savedExpense)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 