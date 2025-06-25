package com.ragl.divide.ui.screens.group

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupEvent
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.data.repositories.GroupRepository
import com.ragl.divide.ui.utils.logMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupViewModel(
    private val groupRepository: GroupRepository
) : ScreenModel {

    private val _group = MutableStateFlow(Group())
    val group: StateFlow<Group> = _group.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    var members by mutableStateOf<List<UserInfo>>(listOf())
        private set
        
    var events by mutableStateOf<List<GroupEvent>>(listOf())
        private set

    private fun updateEvents(events: List<GroupEvent>) {
        this.events = events
    }

    fun setGroup(group: Group, users: List<UserInfo>) {
        screenModelScope.launch {
            members = users
            _group.update { group }
            updateEvents(group.events.values.toList())
        }
    }

    fun refreshGroup(
        onUpdateGroupInState: (String, Group) -> Unit,
        onHandleError: (String) -> Unit,
        onSuccess: () -> Unit = {}
    ) {
        screenModelScope.launch {
            try {
                _isRefreshing.value = true
                logMessage("GroupViewModel", "Refreshing group: ${_group.value.id}")
                
                // Obtener datos frescos del grupo
                val freshGroup = groupRepository.getGroup(_group.value.id)
                
                // Actualizar el estado local
                _group.update { freshGroup }
                updateEvents(freshGroup.events.values.toList())
                
                // Actualizar el cache usando la función pasada
                onUpdateGroupInState(_group.value.id, freshGroup)
                
                logMessage("GroupViewModel", "Group refreshed successfully")
                onSuccess()
            } catch (e: Exception) {
                logMessage("GroupViewModel", "Error refreshing group: ${e.message}")
                onHandleError(e.message ?: "Error al actualizar el grupo")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}