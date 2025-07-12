package com.ragl.divide.presentation.screens.group

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Event
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.services.AppStateService
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.domain.usecases.group.RefreshGroupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupViewModel(
    private val refreshGroupUseCase: RefreshGroupUseCase,
    private val userStateHolder: UserStateHolder,
    private val appStateService: AppStateService
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

    fun refreshGroup() {
        screenModelScope.launch {
            _isRefreshing.value = true
            when (val result = refreshGroupUseCase(_group.value.id)) {
                is RefreshGroupUseCase.Result.Success -> {
                    _group.update { result.group }
                    updateEvents(result.group.events.values.toList())
                    members = userStateHolder.getGroupMembersWithGuests(_group.value.id)
                }

                is RefreshGroupUseCase.Result.Error -> {
                    appStateService.handleError(result.message)
                }
            }
            _isRefreshing.value = false
        }
    }
}