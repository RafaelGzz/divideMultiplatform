package com.ragl.divide.ui.screens.groupExpenseProperties

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.SplitMethod
import com.ragl.divide.data.models.User
import com.ragl.divide.data.repositories.GroupRepository
import com.ragl.divide.ui.utils.Strings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupExpensePropertiesViewModel(
    private val groupRepository: GroupRepository,
    private val strings: Strings
) : ScreenModel {

    private val _group = MutableStateFlow(Group())
    val group = _group.asStateFlow()

    private val _expense = MutableStateFlow(GroupExpense())
    val expense = _expense.asStateFlow()

    var isUpdate = mutableStateOf(false)
    var userId by mutableStateOf("")

    var title by mutableStateOf("")
        private set
    var titleError by mutableStateOf("")
        private set
    var amount by mutableStateOf("")
        private set
    var amountError by mutableStateOf("")
        private set
    var payer by mutableStateOf(User())
        private set
    var splitMethod by mutableStateOf(SplitMethod.EQUALLY)
        private set
    var selectedMembers by mutableStateOf<List<String>>(listOf())
        private set
    var quantities by mutableStateOf<Map<String, Double>>(emptyMap())
        private set
    var percentages by mutableStateOf<Map<String, Int>>(emptyMap())
        private set
    var amountPerPerson by mutableDoubleStateOf(0.0)
        private set

    fun updateTitle(title: String) {
        this.title = title
    }

    fun updateAmount(amount: String) {
        this.amount = amount
    }

    fun updatePayer(user: User) {
        payer = user
    }

    fun updateMethod(splitMethod: SplitMethod) {
        this.splitMethod = splitMethod
    }

    fun updateQuantities(quantities: Map<String, Double>) {
        this.quantities = quantities
    }

    fun updatePercentages(percentages: Map<String, Int>) {
        this.percentages = percentages
    }

    fun updateAmountPerPerson(amountPerPerson: Double) {
        this.amountPerPerson = amountPerPerson
    }

    fun updateSelectedMembers(selectedMembers: List<String>) {
        this.selectedMembers = selectedMembers
    }

    fun setGroupAndExpense(
        group: Group,
        userId: String,
        members: List<User>,
        expense: GroupExpense
    ) {
        screenModelScope.launch {
            if (expense.id.isNotEmpty()) {
                isUpdate.value = true
                _expense.update { expense }
                title = expense.title
                amount = expense.amount.let { if (it == 0.0) "" else it.toString() }
                payer = members.firstOrNull { it.uuid == expense.payers.keys.first() }!!
                splitMethod = expense.splitMethod

                when (expense.splitMethod) {
                    SplitMethod.EQUALLY -> {
                        selectedMembers =
                            expense.debtors.keys.toList() + if (expense.payers.entries.first().value > 0.0) listOf(
                                expense.payers.keys.first()
                            ) else listOf()
                        amountPerPerson = expense.debtors.values.first()
                        percentages = members.associate { it.uuid to 0 }
                        quantities = members.associate { it.uuid to 0.0 }
                    }

                    SplitMethod.PERCENTAGES -> {
                        selectedMembers =
                            expense.debtors.keys.toList() + if (expense.payers.entries.first().value > 0.0) listOf(
                                expense.payers.keys.first()
                            ) else listOf()
                        quantities = members.associate { it.uuid to 0.0 }
                        percentages =
                            expense.debtors.mapValues { it.value.toInt() } + expense.payers.mapValues { it.value.toInt() }
                    }

                    SplitMethod.CUSTOM -> {
                        selectedMembers =
                            expense.debtors.keys.toList() + if (expense.payers.entries.first().value > 0.0) listOf(
                                expense.payers.keys.first()
                            ) else listOf()
                        percentages = members.associate { it.uuid to 0 }
                        quantities =
                            expense.debtors + (payer.uuid to expense.payers[payer.uuid]!!)
                    }
                }
            } else {
                selectedMembers = members.map { it.uuid }
                percentages = members.associate { it.uuid to 0 }
                quantities = members.associate { it.uuid to 0.0 }
                payer = members.first { it.uuid == userId }
            }
            _group.update { group }
        }
        this.userId = userId
    }

    fun validateTitle(): Boolean {
        return when (title.trim()) {
            "" -> {
                this.titleError = strings.getTitleRequired()
                false
            }

            else -> {
                this.titleError = ""
                true
            }
        }
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

    fun saveExpense(
        onSuccess: (GroupExpense) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            if (validateTitle() && validateAmount()) {
                val expense = _expense.value.copy(
                    title = title,
                    amount = amount.toDouble(),
                    payers = when (splitMethod) {
                        SplitMethod.EQUALLY -> mapOf(payer.uuid to if (payer.uuid in selectedMembers) amountPerPerson else 0.0)
                        SplitMethod.PERCENTAGES -> mapOf(payer.uuid to percentages[payer.uuid]!!.toDouble())
                        SplitMethod.CUSTOM -> mapOf(payer.uuid to quantities[payer.uuid]!!)
                    },
                    splitMethod = splitMethod,
                    debtors = when (splitMethod) {
                        SplitMethod.EQUALLY -> selectedMembers.associateWith { amountPerPerson }
                            .filter { it.key != payer.uuid }

                        SplitMethod.PERCENTAGES -> percentages.mapValues { it.value.toDouble() }
                            .filter { it.key != payer.uuid }

                        SplitMethod.CUSTOM -> quantities.filter { it.key != payer.uuid }
                    }
                )
                screenModelScope.launch {
                    val savedExpense = groupRepository.saveExpense(
                        groupId = _group.value.id,
                        expense = expense
                    )
                    onSuccess(savedExpense)
                }
            }
        } catch (e: Exception) {
            onError(e.message.toString())
        }
    }
}