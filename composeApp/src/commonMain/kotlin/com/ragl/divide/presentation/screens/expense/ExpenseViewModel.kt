package com.ragl.divide.presentation.screens.expense

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.domain.services.ScheduleNotificationService
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.domain.usecases.expense.DeleteExpenseUseCase
import com.ragl.divide.domain.usecases.payment.DeleteExpensePaymentUseCase
import com.ragl.divide.domain.usecases.payment.SaveExpensePaymentUseCase
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.logMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ExpenseViewModel(
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val deleteExpensePaymentUseCase: DeleteExpensePaymentUseCase,
    private val saveExpensePaymentUseCase: SaveExpensePaymentUseCase,
    private val scheduleNotificationService: ScheduleNotificationService,
    private val userStateHolder: UserStateHolder,
    private val strings: Strings
) : ScreenModel {

    private val _expense = MutableStateFlow(Expense())
    val expense = _expense.asStateFlow()

    fun setExpenseById(expenseId: String) {
        val expense = userStateHolder.getExpenseById(expenseId)
        _expense.update { expense }
    }

    fun deleteExpense(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        screenModelScope.launch {
            when (val result = deleteExpenseUseCase(_expense.value.id)) {
                is DeleteExpenseUseCase.Result.Success -> {
                    scheduleNotificationService.cancelNotification(_expense.value.id.takeLast(5).toInt())
                    onSuccess()
                }
                is DeleteExpenseUseCase.Result.Error -> {
                    logMessage("DeleteExpenseUseCase", result.exception.message ?: result.exception.stackTraceToString())
                    onFailure(strings.getUnknownError())
                }
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
            _expense.update {
                it.copy(
                    payments = it.payments - paymentId,
                    amountPaid = it.amountPaid - amount,
                    paid = false
                )
            }
            when (val deleteResult =
                deleteExpensePaymentUseCase(_expense.value)) {
                is DeleteExpensePaymentUseCase.Result.Success -> {
                    onSuccess()
                }

                is DeleteExpensePaymentUseCase.Result.Error -> {
                    logMessage("DeleteExpensePaymentUseCase", deleteResult.exception.message ?: deleteResult.exception.stackTraceToString())
                    onFailure(strings.getUnknownError())
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun addPayment(
        amount: Double,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
        onPaidExpense: () -> Unit
    ) {
        screenModelScope.launch {
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
            when (val savePaymentResult = saveExpensePaymentUseCase(_expense.value)) {
                is SaveExpensePaymentUseCase.Result.Success -> {
                    onSuccess()
                }

                is SaveExpensePaymentUseCase.Result.Paid -> {
                    onSuccess()
                    scheduleNotificationService.cancelNotification(_expense.value.id.takeLast(5).toInt())
                    onPaidExpense()
                }

                is SaveExpensePaymentUseCase.Result.Error -> {
                    logMessage("SaveExpensePaymentUseCase", savePaymentResult.exception.message ?: savePaymentResult.exception.stackTraceToString())
                    onFailure(strings.getUnknownError())
                }
            }
        }
    }
}