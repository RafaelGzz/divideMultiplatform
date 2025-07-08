package com.ragl.divide.presentation.screens.groupPayment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.EventPayment
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupPaymentViewModel(
    private val groupRepository: GroupRepository,
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
            this@GroupPaymentViewModel.groupId = groupId
            fromUser = members.firstOrNull { it.uuid == payment.from } ?: UserInfo()
            toUser = members.firstOrNull { it.uuid == payment.to } ?: UserInfo()
        }
    }
    
    fun deletePayment(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        screenModelScope.launch {
            try {
                groupRepository.deleteGroupPayment(groupId, _payment.value)
                userStateHolder.deleteGroupPayment(groupId, _payment.value)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al eliminar el pago")
            }
        }
    }
} 