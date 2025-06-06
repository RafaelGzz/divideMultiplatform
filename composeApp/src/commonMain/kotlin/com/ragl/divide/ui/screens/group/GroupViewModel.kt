package com.ragl.divide.ui.screens.group

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupEvent
import com.ragl.divide.data.models.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupViewModel() : ScreenModel {

    private val _group = MutableStateFlow(Group())
    val group: StateFlow<Group> = _group.asStateFlow()

    var expensesAndPayments by mutableStateOf<List<Any>>(listOf())
        private set

    var members by mutableStateOf<List<UserInfo>>(listOf())
        private set
        
    var events by mutableStateOf<List<GroupEvent>>(listOf())
        private set

    private fun updateExpensesAndPayments(expensesAndPayments: List<Any>) {
        this.expensesAndPayments = expensesAndPayments
    }
    
    private fun updateEvents(events: List<GroupEvent>) {
        this.events = events
    }

    fun setGroup(group: Group, users: List<UserInfo>) {
        screenModelScope.launch {
            members = users
            _group.update { group }
            updateExpensesAndPayments(group.expenses.values.toList() + group.payments.values.toList())
            updateEvents(group.events.values.toList())
        }
    }
}