package com.ragl.divide.ui.screens.group

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.User
import com.ragl.divide.data.repositories.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupViewModel(
    private val groupRepository: GroupRepository
) : ScreenModel {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _group = MutableStateFlow(Group())
    val group: StateFlow<Group> = _group.asStateFlow()

    var expensesAndPayments by mutableStateOf<List<Any>>(listOf())
        private set

    var members by mutableStateOf<List<User>>(listOf())
        private set
        
    var currentUserId by mutableStateOf("")
        private set

    private fun updateExpensesAndPayments(expensesAndPayments: List<Any>) {
        this.expensesAndPayments = expensesAndPayments
    }

    fun setGroup(group: Group, userId: String) {
        screenModelScope.launch {
            _isLoading.update { true }
            _group.update {
                group
            }
            currentUserId = userId
            members = groupRepository.getUsers(_group.value.users.values)
            updateExpensesAndPayments(group.expenses.values.toList() + group.payments.values.toList())
            _isLoading.update { false }
        }
    }

    fun getPaidByNames(paidBy: List<String>): String {
        return paidBy.map { uid ->
            members.find { it.uuid == uid }?.name
        }.joinToString(", ") // Une los nombres con comas
    }
}