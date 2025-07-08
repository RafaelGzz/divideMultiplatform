package com.ragl.divide.presentation.screens.event

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Event
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.utils.logMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventViewModel(
    private val groupRepository: GroupRepository,
    private val userStateHolder: UserStateHolder
) : ScreenModel {

    private val _event = MutableStateFlow(Event())
    val event: StateFlow<Event> = _event.asStateFlow()

    val uuid = userStateHolder.getUUID()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    var expensesAndPayments by mutableStateOf<List<Any>>(listOf())
        private set

    var members by mutableStateOf<List<UserInfo>>(listOf())
        private set

    private var currentGroupId: String = ""

    private fun updateExpensesAndPayments(expensesAndPayments: List<Any>) {
        this.expensesAndPayments = expensesAndPayments
    }

    fun setEvent(groupId: String, eventId: String) {
        val event = userStateHolder.getEventById(groupId, eventId)
        members = userStateHolder.getGroupMembersWithGuests(groupId)
        currentGroupId = groupId
        _event.update { event }
        updateExpensesAndPayments(event.expenses.values.toList() + event.payments.values.toList())
    }

    fun getPaidByNames(paidBy: List<String>): String {
        return paidBy.mapNotNull { uid ->
            members.find { it.uuid == uid }?.name
        }.joinToString(", ")
    }

    fun refreshEvent(
        onError: (String) -> Unit
    ) {
        screenModelScope.launch {
            try {
                _isRefreshing.value = true
                logMessage(
                    "EventViewModel",
                    "Refreshing event: ${_event.value.id} in group: $currentGroupId"
                )

                val freshEvent = groupRepository.getEvent(currentGroupId, _event.value.id)

                _event.update { freshEvent }
                updateExpensesAndPayments(freshEvent.expenses.values.toList() + freshEvent.payments.values.toList())
                members = userStateHolder.getGroupMembersWithGuests(currentGroupId)
                userStateHolder.updateEventInState(currentGroupId, _event.value.id, freshEvent)

                logMessage("EventViewModel", "Event refreshed successfully")
            } catch (e: Exception) {
                logMessage("EventViewModel", "Error refreshing event: ${e.message}")
                onError(e.message ?: "Error al actualizar el evento")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun settleEvent(groupId: String, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                groupRepository.settleEvent(groupId, _event.value.id)
                _event.update { it.copy(settled = true) }
                userStateHolder.settleEvent(groupId, _event.value.id)
            } catch (e: Exception) {
                onError(e.message.toString())
            }
        }
    }

    fun reopenEvent(groupId: String, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                _event.update {
                    it.copy(
                        settled = false
                    )
                }
                groupRepository.reopenEvent(groupId, _event.value.id)
                userStateHolder.reopenEvent(groupId, _event.value.id)
            } catch (e: Exception) {
                onError(e.message.toString())
            }
        }
    }
} 