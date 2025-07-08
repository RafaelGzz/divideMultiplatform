package com.ragl.divide.presentation.screens.group

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Event
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.logMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupViewModel(
    private val groupRepository: GroupRepository,
    private val userStateHolder: UserStateHolder,
    private val strings: Strings
) : ScreenModel {

    private val _group = MutableStateFlow(Group())
    val group: StateFlow<Group> = _group.asStateFlow()

    var uuid = userStateHolder.getUUID()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    var members by mutableStateOf<List<UserInfo>>(listOf())
        private set

    var events by mutableStateOf<List<Event>>(listOf())
        private set

    private fun updateEvents(events: List<Event>) {
        this.events = events
    }

    fun setGroup(groupId: String) {
        screenModelScope.launch {
            members = userStateHolder.getGroupMembersWithGuests(groupId)
            _group.update { userStateHolder.getGroupById(groupId) }
            updateEvents(_group.value.events.values.toList())
        }
    }

    fun refreshGroup(onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                _isRefreshing.value = true
                logMessage("GroupViewModel", "Refreshing group: ${_group.value.id}")

                val freshGroup = groupRepository.getGroup(_group.value.id)

                _group.update { freshGroup }
                updateEvents(freshGroup.events.values.toList())

                userStateHolder.updateGroupInState(_group.value.id, freshGroup)
                members = userStateHolder.getGroupMembersWithGuests(_group.value.id)

                logMessage("GroupViewModel", "Group refreshed successfully")
            } catch (e: Exception) {
                logMessage("GroupViewModel", "Error refreshing group: ${e.message}")
                onError(e.message ?: strings.getUnknownError())
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}