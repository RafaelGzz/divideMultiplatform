package com.ragl.divide.ui.screens.event

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.GroupEvent
import com.ragl.divide.data.models.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventViewModel() : ScreenModel {
    
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

    fun deleteEvent() {
        screenModelScope.launch {

        }
    }
    
    // Método para liquidar un evento si es necesario
    fun settleEvent() {
        screenModelScope.launch {
            val updatedEvent = _event.value.copy(
                settled = true,
                expenses = _event.value.expenses.mapValues { (_, expense) -> 
                    expense.copy(settled = true) 
                },
                payments = _event.value.payments.mapValues { (_, payment) -> 
                    payment.copy(settled = true) 
                }
            )
            _event.update { updatedEvent }
            // Aquí se llamaría al repositorio para guardar el evento liquidado
        }
    }
    
    // Método para reabrir un evento liquidado
    fun reopenEvent() {
        screenModelScope.launch {
            val updatedEvent = _event.value.copy(
                settled = false,
                expenses = _event.value.expenses.mapValues { (_, expense) -> 
                    expense.copy(settled = false) 
                },
                payments = _event.value.payments.mapValues { (_, payment) -> 
                    payment.copy(settled = false) 
                }
            )
            _event.update { updatedEvent }
            // Aquí se llamaría al repositorio para guardar el evento reabierto
        }
    }
} 