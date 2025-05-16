package com.ragl.divide.ui.screens.groupExpense

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateMap
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.User
import com.ragl.divide.data.repositories.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupExpenseViewModel(
    private val groupRepository: GroupRepository,
) : ScreenModel {
    private val _groupExpense = MutableStateFlow(GroupExpense())
    val groupExpense = _groupExpense.asStateFlow()

    private var _sortedDebtors = MutableStateFlow(mutableMapOf<User, Double>())
    val sortedDebtors = _sortedDebtors.asStateFlow()

    private var _sortedPaidBy = MutableStateFlow(mutableMapOf<User, Double>())
    val sortedPaidBy = _sortedPaidBy.asStateFlow()

    var members: List<User> = mutableStateListOf()
    
    fun setGroupExpense(groupExpense: GroupExpense, users: List<User>) {
        _groupExpense.update {
            groupExpense
        }
        members = users
        _sortedDebtors.update {
            groupExpense.debtors.entries.filter { it.value != 0.0 }
                .mapNotNull { (memberId, debt) ->
                    members.find { it.uuid == memberId }?.let { member -> // Use let for conciseness
                        member to debt // Create a Pair of (member, debt)
                    }
                }
                .sortedBy { (member, _) -> member.name.lowercase() }.toMutableStateMap()
        }
        _sortedPaidBy.update {
            groupExpense.payers.entries.filter { it.value != 0.0 }
                .mapNotNull { (memberId, debt) ->
                    members.find { it.uuid == memberId }?.let { member -> // Use let for conciseness
                        member to debt // Create a Pair of (member, debt)
                    }
                }
                .sortedBy { (member, _) -> member.name.lowercase() }.toMutableStateMap()
        }
    }

    fun deleteExpense(groupId: String, onSuccess: (GroupExpense) -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                groupRepository.deleteExpense(groupId, groupExpense.value)
                onSuccess(groupExpense.value)
            } catch (e: Exception) {
                onError(e.message ?: "An error occurred")
            }
        }
    }
}