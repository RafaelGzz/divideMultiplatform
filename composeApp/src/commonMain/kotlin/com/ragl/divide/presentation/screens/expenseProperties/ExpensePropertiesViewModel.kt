package com.ragl.divide.presentation.screens.expenseProperties

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Category
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Frequency
import com.ragl.divide.data.models.Payment
import com.ragl.divide.domain.services.ScheduleNotificationService
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.domain.usecases.expense.SaveExpenseUseCase
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.logMessage
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ExpensePropertiesViewModel(
    private val saveExpenseUseCase: SaveExpenseUseCase,
    private val userStateHolder: UserStateHolder,
    private val scheduleNotificationService: ScheduleNotificationService,
    private val strings: Strings
) : ScreenModel {
    private var id = ""
    var amountPaid by mutableDoubleStateOf(0.0)
        private set
    var title by mutableStateOf("")
        private set
    var titleError: String? by mutableStateOf("")
        private set
    var amount by mutableStateOf("")
        private set
    var amountError by mutableStateOf("")
        private set
    var category by mutableStateOf(Category.GENERAL)
        private set
    var notes by mutableStateOf("")
        private set
    var isRemindersEnabled by mutableStateOf(false)
        private set
    var frequency by mutableStateOf(Frequency.DAILY)
        private set
    var startingDate by mutableLongStateOf(Clock.System.now().toEpochMilliseconds())
        private set
    var addedDate by mutableLongStateOf(0L)
        private set
    var payments by mutableStateOf(emptyMap<String, Payment>())
        private set
    var paid by mutableStateOf(false)
        private set

    var reminderPermissionMessageDialogEnabled by mutableStateOf(false)
        private set

    var notificationPermissionRejectedDialogEnabled by mutableStateOf(false)
        private set

    fun handleReminderPermissionCheck(checked: Boolean) {
        when (checked) {
            true -> {
                val hasExactAlarmPermission = scheduleNotificationService.canScheduleExactAlarms()
                val hasNotificationPermission =
                    scheduleNotificationService.hasNotificationPermission()

                if (hasExactAlarmPermission && hasNotificationPermission) {
                    updateIsRemindersEnabled(true)
                } else {
                    if (!hasNotificationPermission &&
                        scheduleNotificationService.wasNotificationPermissionRejectedPermanently()
                    ) {
                        notificationPermissionRejectedDialogEnabled = true
                    } else {
                        // Solicitar permisos faltantes automáticamente
                        if (!hasNotificationPermission) {
                            scheduleNotificationService.requestNotificationPermission()
                        }
                        if (!hasExactAlarmPermission) {
                            reminderPermissionMessageDialogEnabled = true
                        }
                    }
                }
            }

            false -> {
                updateIsRemindersEnabled(false)
            }
        }
    }

    fun onPermissionDialogDismiss() {
        reminderPermissionMessageDialogEnabled = false
    }

    fun onPermissionDialogConfirm() {
        reminderPermissionMessageDialogEnabled = false
        scheduleNotificationService.requestScheduleExactAlarmPermission()
    }

    fun onNotificationPermissionRejectedDialogDismiss() {
        notificationPermissionRejectedDialogEnabled = false
    }

    fun onNotificationPermissionRejectedDialogConfirm() {
        onNotificationPermissionRejectedDialogDismiss()
        scheduleNotificationService.requestNotificationPermission()
    }

    fun showNotificationPermissionRejectedDialog() {
        notificationPermissionRejectedDialogEnabled = true
    }

    fun updateTitle(title: String) {
        this.title = title
    }

    fun validateTitle(): Boolean {
        return when (title.trim()) {
            "" -> {
                this.titleError = strings.getTitleRequired()
                false
            }

            else -> {
                this.titleError = null
                true
            }
        }
    }

    fun updateAmount(amount: String) {
        this.amount = amount
    }

    fun validateAmount(): Boolean {
        if (amount.isEmpty()) {
            this.amountError = strings.getAmountRequired()
            return false
        }

        val amountDouble = amount.toDoubleOrNull() ?: run {
            this.amountError = strings.getInvalidAmount()
            return false
        }

        if (amountDouble <= 0) {
            this.amountError = strings.getAmountMustBeGreater()
            return false
        }

        this.amountError = ""
        return true
    }

    fun updateCategory(category: Category) {
        this.category = category
    }

    fun updateNotes(notes: String) {
        this.notes = notes
    }

    private fun updateIsRemindersEnabled(isRemindersEnabled: Boolean) {
        this.isRemindersEnabled = isRemindersEnabled
    }

    fun updateFrequency(frequency: Frequency) {
        this.frequency = frequency
    }

    fun updateStartingDate(startingDate: Long) {
        this.startingDate = startingDate
    }

    fun saveExpense(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (validateTitle().and(validateAmount())) {
            screenModelScope.launch {
                val expense = Expense(
                    id = id,
                    title = title.trim(),
                    amount = amount.toDouble(),
                    category = category,
                    reminders = isRemindersEnabled,
                    payments = payments,
                    notes = notes.trim(),
                    frequency = frequency,
                    startingDate = startingDate,
                    amountPaid = amountPaid,
                    createdAt = if (addedDate == 0L) Clock.System.now()
                        .toEpochMilliseconds() else addedDate,
                    paid = paid
                )

                when(val result = saveExpenseUseCase(expense)){
                    is SaveExpenseUseCase.Result.Success -> {
                        onSuccess()
                    }
                    is SaveExpenseUseCase.Result.Error -> {
                        logMessage("SaveExpenseUseCase", result.exception.message ?: result.exception.stackTraceToString())
                        onError(strings.getUnknownError())
                    }
                }
            }
        }
    }

    fun setViewModelExpense(expenseId: String) {
        val expense = userStateHolder.getExpenseById(expenseId)
        id = expense.id
        title = expense.title
        amount = expense.amount.toString()
        category = expense.category
        payments = expense.payments
        notes = expense.notes
        frequency = expense.frequency
        startingDate = expense.startingDate
        amountPaid = expense.amountPaid
        addedDate = expense.createdAt
        paid = expense.paid
        if (scheduleNotificationService.hasNotificationPermission() && scheduleNotificationService.canScheduleExactAlarms())
            isRemindersEnabled = expense.reminders
    }
}