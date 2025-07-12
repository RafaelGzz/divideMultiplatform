package com.ragl.divide.presentation.screens.eventPayment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.EventPayment
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.domain.usecases.eventPayment.DeleteEventPaymentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventPaymentViewModel(
    private val deleteEventPaymentUseCase: DeleteEventPaymentUseCase,
    private val userStateHolder: UserStateHolder
) : ScreenModel {
    
    private val _payment = MutableStateFlow(EventPayment())
    val payment = _payment.asStateFlow()

    var fromUser by mutableStateOf(UserInfo())
        private set
    
    var toUser by mutableStateOf(UserInfo())
        private set
        
    var groupId by mutableStateOf("")
        private set
    
    fun setPayment(groupId: String, paymentId: String, eventId: String) {
        screenModelScope.launch {
            val payment = userStateHolder.getEventPaymentById(groupId, paymentId, eventId)
            val members = userStateHolder.getGroupMembersWithGuests(groupId)

            _payment.update { payment }
            this@EventPaymentViewModel.groupId = groupId
            fromUser = members.firstOrNull { it.uuid == payment.from } ?: UserInfo()
            toUser = members.firstOrNull { it.uuid == payment.to } ?: UserInfo()
        }
    }
    
    fun deletePayment(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        screenModelScope.launch {
            when(val result = deleteEventPaymentUseCase.invoke(groupId, _payment.value)) {
                is DeleteEventPaymentUseCase.Result.Success -> {
                    onSuccess()
                }
                is DeleteEventPaymentUseCase.Result.Error -> {
                    onError(result.message)
                }
            }
        }
    }
} 