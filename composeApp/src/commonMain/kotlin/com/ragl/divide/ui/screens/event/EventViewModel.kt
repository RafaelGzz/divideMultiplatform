package com.ragl.divide.ui.screens.event

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.GroupEvent
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.data.repositories.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventViewModel(
    val groupRepository: GroupRepository
) : ScreenModel {

    private val _event = MutableStateFlow(GroupEvent())
    val event: StateFlow<GroupEvent> = _event.asStateFlow()

    var expensesAndPayments by mutableStateOf<List<Any>>(listOf())
        private set

    var members by mutableStateOf<List<UserInfo>>(listOf())
        private set

    private fun updateExpensesAndPayments(expensesAndPayments: List<Any>) {
        this.expensesAndPayments = expensesAndPayments
    }

    fun setEvent(event: GroupEvent, memberList: List<UserInfo>) {
        screenModelScope.launch {
            members = memberList
            _event.update { event }
            updateExpensesAndPayments(event.expenses.values.toList() + event.payments.values.toList())
        }
    }

    fun getPaidByNames(paidBy: List<String>): String {
        return paidBy.mapNotNull { uid ->
            members.find { it.uuid == uid }?.name
        }.joinToString(", ")
    }

    fun settleEvent(groupId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                groupRepository.settleEvent(groupId, event.value.id)
                _event.update {
                    it.copy(
                        settled = true
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message.toString())
            }
        }
    }

    fun reopenEvent(groupId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                _event.update {
                    it.copy(
                        settled = false
                    )
                }
                groupRepository.reopenEvent(groupId, event.value.id)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message.toString())
            }
        }
    }
} 