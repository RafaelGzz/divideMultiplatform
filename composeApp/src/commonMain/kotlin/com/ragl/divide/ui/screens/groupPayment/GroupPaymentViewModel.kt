package com.ragl.divide.ui.screens.groupPayment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.GroupPayment
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.data.repositories.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupPaymentViewModel(
    private val groupRepository: GroupRepository
) : ScreenModel {
    
    private val _payment = MutableStateFlow(GroupPayment())
    val payment = _payment.asStateFlow()

    var fromUser by mutableStateOf(UserInfo())
        private set
    
    var toUser by mutableStateOf(UserInfo())
        private set
        
    var groupId by mutableStateOf("")
        private set
    
    fun setPayment(payment: GroupPayment, groupId: String, members: List<UserInfo>) {
        screenModelScope.launch {
            _payment.update { payment }
            this@GroupPaymentViewModel.groupId = groupId
            fromUser = members.firstOrNull { it.uuid == payment.from } ?: UserInfo()
            toUser = members.firstOrNull { it.uuid == payment.to } ?: UserInfo()
        }
    }
    
    fun deletePayment(
        onSuccess: (GroupPayment) -> Unit,
        onError: (String) -> Unit
    ) {
        screenModelScope.launch {
            try {
                groupRepository.deleteGroupPayment(groupId, _payment.value)
                onSuccess(_payment.value)
            } catch (e: Exception) {
                onError(e.message ?: "Error al eliminar el pago")
            }
        }
    }
} 