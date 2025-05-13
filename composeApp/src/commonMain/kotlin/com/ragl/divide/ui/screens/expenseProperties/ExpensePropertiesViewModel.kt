package com.ragl.divide.ui.screens.expenseProperties

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
import com.ragl.divide.data.repositories.UserRepository
import com.ragl.divide.data.services.ScheduleNotificationService
import com.ragl.divide.ui.utils.Strings
import com.ragl.divide.ui.utils.logMessage
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class ExpensePropertiesViewModel(
    private val userRepository: UserRepository,
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
    var payments by mutableStateOf("1")
        private set
    var paymentsError by mutableStateOf("")
        private set
    var notes by mutableStateOf("")
        private set
    var isRemindersEnabled by mutableStateOf(false)
        private set
    var frequency by mutableStateOf(Frequency.DAILY)
        private set
    var startingDate by mutableLongStateOf(Clock.System.now().toEpochMilliseconds())
        private set

    var reminderPermissionMessageDialogEnabled by mutableStateOf(false)
        private set

    fun handleReminderPermissionCheck(checked: Boolean) {
        updateIsRemindersEnabled(false)
        return
        when (checked) {
            true -> {
                if (scheduleNotificationService.canScheduleExactAlarms()) {
                    updateIsRemindersEnabled(true)
                } else {
                    reminderPermissionMessageDialogEnabled = true
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

    fun updateTitle(title: String) {
        this.title = title
    }

    private fun validateTitle(): Boolean {
        return when (title.trim()) {
            "" -> {
                this.titleError = "Title is required"
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

    private fun validateAmount(): Boolean {
        if (amount.isEmpty()) {
            this.amountError = "Amount is required"
            return false
        }

        val amountDouble = amount.toDoubleOrNull() ?: run {
            this.amountError = "Invalid amount"
            return false
        }

        if (amountDouble <= 0) {
            this.amountError = "Amount must be greater than 0"
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

    fun updatePayments(payments: String) {
        if (payments.isNotEmpty() &&
            //payments.isDigitsOnly() &&
            payments.toInt() <= 0
        ) this.payments =
            "1"
        else this.payments = payments
    }

    private fun validatePayments(): Boolean {
        return when (payments.trim()) {
            "" -> {
                this.paymentsError = "Payments is required"
                false
            }

            else -> {
                this.paymentsError = ""
                true
            }
        }
    }

    fun updateIsRemindersEnabled(isRemindersEnabled: Boolean) {
        this.isRemindersEnabled = isRemindersEnabled
    }

    fun updateFrequency(frequency: Frequency) {
        this.frequency = frequency
    }

    fun updateStartingDate(startingDate: Long) {
        this.startingDate = startingDate
    }

    fun saveExpense(
        onSuccess: (Expense) -> Unit,
        onError: (String) -> Unit
    ) {
        val valid = validateTitle().and(validateAmount()).and(validatePayments())
        if (valid) {
            screenModelScope.launch {
                try {
                    val savedExpense = userRepository.saveExpense(
                        Expense(
                            id = id,
                            title = title.trim(),
                            amount = amount.toDouble(),
                            category = category,
                            reminders = isRemindersEnabled,
                            numberOfPayments = payments.toInt(),
                            payments = if (id.isNotEmpty()) userRepository.getExpensePayments(id) else emptyMap(),
                            notes = notes.trim(),
                            frequency = frequency,
                            startingDate = startingDate
                        )
                    )
                    scheduleNotificationService.cancelNotification(savedExpense.id.takeLast(5).toInt())
                    if (isRemindersEnabled) {
                        scheduleNotificationService.scheduleNotification(
                            id = savedExpense.id.takeLast(5).toInt(),
                            title = strings.getNotificationTitleString(title),
                            message = strings.getNotificationBodyString(),
                            startingDateMillis = startingDate,
                            frequency = frequency
                        )
                    }
                    onSuccess(savedExpense)
                } catch (e: Exception) {
                    logMessage("ExpenseViewModel", e.stackTraceToString() ?: "Unknown error")
                    //Log.e("ExpenseViewModel", e.message, e)
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun setViewModelExpense(expense: Expense) {
        screenModelScope.launch {
            id = expense.id
            title = expense.title
            amount = expense.amount.toString()
            category = expense.category
            isRemindersEnabled = expense.reminders
            payments = expense.numberOfPayments.toString()
            notes = expense.notes
            frequency = expense.frequency
            startingDate = expense.startingDate
            amountPaid = expense.amountPaid
        }
    }
}