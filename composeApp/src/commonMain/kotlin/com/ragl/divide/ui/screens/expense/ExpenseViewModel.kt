package com.ragl.divide.ui.screens.expense

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.repositories.UserRepository
import com.ragl.divide.data.services.ScheduleNotificationService
import com.ragl.divide.ui.utils.Strings
import com.ragl.divide.ui.utils.logMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ExpenseViewModel(
    private val userRepository: UserRepository,
    private val scheduleNotificationService: ScheduleNotificationService,
    private val strings: Strings
) : ScreenModel {

    private val _expense = MutableStateFlow(Expense())
    val expense = _expense.asStateFlow()

    fun setExpense(expense: Expense) {
        screenModelScope.launch {
            _expense.update {
                expense
            }
        }
    }

    fun deleteExpense(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        screenModelScope.launch {
            try {
                userRepository.deleteExpense(_expense.value.id)
                scheduleNotificationService.cancelNotification(
                    _expense.value.id.takeLast(5).toInt()
                )
                onSuccess()
            } catch (e: Exception) {
                logMessage("ExpenseViewModel", e.message ?: "")
                onFailure(e.message ?: strings.getSomethingWentWrong())
            }
        }
    }

    fun deletePayment(
        paymentId: String,
        amount: Double,
        onFailure: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        screenModelScope.launch {
            try {
                _expense.update {
                    it.copy(
                        payments = it.payments - paymentId,
                        amountPaid = it.amountPaid - amount,
                        paid = false
                    )
                }
                userRepository.saveExpense(_expense.value)
                //userRepository.deleteExpensePayment(paymentId, amount, _expense.value.id)
                onSuccess()
            } catch (e: Exception) {
                logMessage("ExpenseViewModel", e.message ?: "")
                onFailure(e.message ?: strings.getErrorDeletingPayment())
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun addPayment(
        amount: Double,
        onSuccess: (Payment) -> Unit,
        onFailure: (String) -> Unit,
        onPaidExpense: () -> Unit
    ) {
        screenModelScope.launch {
            try {
                val id = "id${Clock.System.now().toEpochMilliseconds()}"
                val newPayment = Payment(
                    amount = amount,
                    id = id
                )
                _expense.update {
                    it.copy(
                        payments = it.payments + (id to newPayment),
                        amountPaid = it.amountPaid + amount,
                        paid = (it.amountPaid + amount) == it.amount
                    )
                }
                userRepository.saveExpense(_expense.value)
//                val savedPayment = userRepository.saveExpensePayment(
//                    Payment(amount = amount),
//                    expenseId = _expense.value.id,
//                    expensePaid = _expense.value.amountPaid + amount == _expense.value.amount
//                )
                onSuccess(newPayment)
                if (_expense.value.paid) {
                    scheduleNotificationService.cancelNotification(
                        _expense.value.id.takeLast(5).toInt()
                    )
                    onPaidExpense()
                }
            } catch (e: Exception) {
                logMessage("ExpenseViewModel", e.message ?: "")
                onFailure(e.message ?: strings.getUnknownError())
            }
        }
    }
}