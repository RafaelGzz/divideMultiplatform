package com.ragl.divide.domain.usecases.expense

import com.ragl.divide.data.models.Expense
import com.ragl.divide.domain.repositories.UserRepository

class GetExpenseUseCase(
    private val userRepository: UserRepository
) {
    sealed class Result {
        data class Success(val expense: Expense) : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(expenseId: String): Result {
        return try {
            val expense = userRepository.getExpense(expenseId)
            Result.Success(expense)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}