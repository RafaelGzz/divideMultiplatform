package com.ragl.divide.domain.usecases.payment

import com.ragl.divide.data.models.Expense
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
class DeleteExpensePaymentUseCase(
    private val userRepository: UserRepository,
    private val userStateHolder: UserStateHolder,
) {
    sealed class Result {
        object Success : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(paymentId: String, amount: Double, expense: Expense): Result {
        return try {
            userRepository.saveExpense(expense)
            userStateHolder.saveExpense(expense)
            Result.Success
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 