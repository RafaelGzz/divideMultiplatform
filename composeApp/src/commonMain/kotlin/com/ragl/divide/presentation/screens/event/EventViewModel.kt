package com.ragl.divide.presentation.screens.event

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Event
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.services.AppStateService
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.domain.usecases.event.RefreshEventUseCase
import com.ragl.divide.domain.usecases.event.ReopenEventUseCase
import com.ragl.divide.domain.usecases.event.SettleEventUseCase
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.logMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventViewModel(
    private val refreshEventUseCase: RefreshEventUseCase,
    private val settleEventUseCase: SettleEventUseCase,
    private val reopenEventUseCase: ReopenEventUseCase,
    private val userStateHolder: UserStateHolder,
    private val appStateService: AppStateService,
    private val strings: Strings
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
    ) {
        screenModelScope.launch {
            _isRefreshing.value = true
            when (val result = refreshEventUseCase(currentGroupId, _event.value.id)) {
                is RefreshEventUseCase.Result.Success -> {
                    _event.update { result.event }
                    updateExpensesAndPayments(result.event.expenses.values.toList() + result.event.payments.values.toList())
                    members = userStateHolder.getGroupMembersWithGuests(currentGroupId)
                }
                is RefreshEventUseCase.Result.Error -> {
                    logMessage("RefreshEventUseCase", result.exception.message ?: result.exception.stackTraceToString())
                    appStateService.handleError(strings.getUnknownError())
                }
            }
            _isRefreshing.value = false
        }
    }

    fun settleEvent(groupId: String) {
        screenModelScope.launch {
            when (val result = settleEventUseCase(groupId, _event.value.id)) {
                is SettleEventUseCase.Result.Success -> {
                    _event.update { it.copy(settled = true) }
                }
                is SettleEventUseCase.Result.Error -> {
                    logMessage("SettleEventUseCase", result.exception.message ?: result.exception.stackTraceToString())
                    appStateService.handleError(strings.getUnknownError())
                }
            }
        }
    }

    fun reopenEvent(groupId: String) {
        screenModelScope.launch {
            when (val result = reopenEventUseCase(groupId, _event.value.id)) {
                is ReopenEventUseCase.Result.Success -> {
                    _event.update { it.copy(settled = false) }
                }
                is ReopenEventUseCase.Result.Error -> {
                    logMessage("ReopenEventUseCase", result.exception.message ?: result.exception.stackTraceToString())
                    appStateService.handleError(strings.getUnknownError())
                }
            }
        }
    }
} 