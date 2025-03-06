package com.ragl.divide.ui.screens.expense

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.repositories.UserRepository
import com.ragl.divide.ui.utils.logMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val userRepository: UserRepository
) : ScreenModel {

    private val _expense = MutableStateFlow(Expense())
    val expense = _expense.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun setExpense(expense: Expense) {
        screenModelScope.launch {
            _expense.update {
                expense
            }
        }
    }

    fun deleteExpense(id: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        screenModelScope.launch {
            try {
                userRepository.deleteExpense(id)
                onSuccess()
            } catch (e: Exception) {
                logMessage("ExpenseViewModel", e.message ?: "")
                onFailure(e.message ?: "Something went wrong")
            }
        }
    }
    
    fun updateExpense(expense: Expense, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        screenModelScope.launch {
            try {
                userRepository.saveExpense(expense)
                _expense.update { expense }
                onSuccess()
            } catch (e: Exception) {
                logMessage("ExpenseViewModel", e.message ?: "")
                onFailure(e.message ?: "Something went wrong")
            }
        }
    }   

    fun deletePayment(paymentId: String, amount: Double, onFailure: (String) -> Unit, onSuccess: () -> Unit) {
        screenModelScope.launch {
            try {
                userRepository.deleteExpensePayment(paymentId, amount, _expense.value.id)
                _expense.update {
                    it.copy(
                        payments = userRepository.getExpensePayments(_expense.value.id),
                        amountPaid = it.amountPaid - amount
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                logMessage("ExpenseViewModel", e.message ?: "")
                onFailure(e.message ?: "Something went wrong")
            }
        }
    }

    fun addPayment(amount: Double, onSuccess: (Payment) -> Unit, onFailure: (String) -> Unit, onPaidExpense: () -> Unit) {
        screenModelScope.launch {
            try {
                val savedPayment = userRepository.saveExpensePayment(
                    Payment(amount = amount),
                    expenseId = _expense.value.id,
                    expensePaid = _expense.value.amountPaid + amount == _expense.value.amount
                )
                onSuccess(savedPayment)
                if (_expense.value.amountPaid + amount == _expense.value.amount) onPaidExpense()
                else _expense.update {
                    it.copy(
                        payments = userRepository.getExpensePayments(_expense.value.id),
                        amountPaid = it.amountPaid + amount,
                        paid = (it.amountPaid + amount) == it.amount
                    )
                }
            } catch (e: Exception) {
                logMessage("ExpenseViewModel", e.message ?: "")
                onFailure(e.message ?: "Something went wrong")
            }
        }
    }
}