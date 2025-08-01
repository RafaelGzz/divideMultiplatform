package com.ragl.divide.presentation.screens.eventExpense

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateMap
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.domain.usecases.eventExpense.DeleteEventExpenseUseCase
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.logMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventExpenseViewModel(
    private val deleteEventExpenseUseCase: DeleteEventExpenseUseCase,
    private val userStateHolder: UserStateHolder,
    private val strings: Strings,
) : ScreenModel {
    private val _eventExpense = MutableStateFlow(EventExpense())
    val groupExpense = _eventExpense.asStateFlow()

    private var _sortedDebtors = MutableStateFlow(mutableMapOf<UserInfo, Double>())
    val sortedDebtors = _sortedDebtors.asStateFlow()

    private var _sortedPaidBy = MutableStateFlow(mutableMapOf<UserInfo, Double>())
    val sortedPaidBy = _sortedPaidBy.asStateFlow()

    var members: List<UserInfo> = mutableStateListOf()

    fun setGroupExpense(groupId: String, expenseId: String, eventId: String) {
        val groupMembers = userStateHolder.getGroupMembersWithGuests(groupId)
        val eventExpense = userStateHolder.getEventExpenseById(groupId, expenseId, eventId)

        _eventExpense.update { eventExpense }
        members = groupMembers
        _sortedDebtors.update {
            eventExpense.debtors.entries.filter { it.value != 0.0 }
                .mapNotNull { (memberId, debt) ->
                    members.find { it.uuid == memberId }?.let { member ->
                        member to debt
                    }
                }
                .sortedBy { (member, _) -> member.name.lowercase() }.toMutableStateMap()
        }
        _sortedPaidBy.update {
            eventExpense.payers.entries
                .mapNotNull { (memberId, debt) ->
                    members.find { it.uuid == memberId }?.let { member ->
                        member to debt
                    }
                }
                .sortedBy { (member, _) -> member.name.lowercase() }.toMutableStateMap()
        }
    }

    fun deleteExpense(groupId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            when(val result = deleteEventExpenseUseCase(groupId, _eventExpense.value)){
                is DeleteEventExpenseUseCase.Result.Success -> {
                    onSuccess()
                }
                is DeleteEventExpenseUseCase.Result.Error -> {
                    logMessage("DeleteEventExpenseUseCase", result.exception.message ?: result.exception.stackTraceToString())
                    onError(strings.getUnknownError())
                }
            }
        }
    }
}