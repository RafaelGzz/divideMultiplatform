package com.ragl.divide.ui.screens.groupProperties

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupUser
import com.ragl.divide.data.models.User
import com.ragl.divide.data.repositories.GroupRepository
import com.ragl.divide.ui.utils.logMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de propiedades del grupo, enfocado en la edición de información
 * del grupo como nombre, descripción, imagen, y gestión de miembros.
 */
class GroupPropertiesViewModel(
    private val groupRepository: GroupRepository
) : ScreenModel {

    private var _group = MutableStateFlow(Group())
    var group = _group.asStateFlow()

    var members by mutableStateOf<List<User>>(listOf())
        private set

    var nameError by mutableStateOf("")
        private set

    private var _isLoading = MutableStateFlow(false)
    var isLoading = _isLoading.asStateFlow()

    fun updateName(name: String) {
        _group.update {
            it.copy(name = name)
        }
    }

    fun addUser(userId: String) {
        _group.update {
            it.copy(users = it.users + (userId to GroupUser(id = userId)))
        }
    }

    fun addMember(member: User) {
        this.members += member
    }

    fun removeUser(userId: String) {
        _group.update {
            it.copy(users = it.users - userId)
        }
    }

    fun setGroup(group: Group) {
        screenModelScope.launch {
            _isLoading.update { true }
            _group.update {
                group
            }
            members = groupRepository.getUsers(_group.value.users.values.map { it.id })
            _isLoading.update { false }
        }
    }

    private fun updateMembers(members: List<User>) {
        this.members = members
    }

    fun leaveGroup(onSuccessful: () -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                groupRepository.leaveGroup(_group.value.id)
                onSuccessful()
            } catch (e: Exception) {
                logMessage("GroupDetailsViewModel", e.toString())
                onError(e.message ?: "An error occurred")
            }
        }
    }

    private fun validateName(): Boolean {
        return when (_group.value.name) {
            "" -> {
                this.nameError = "Title is required"
                false
            }

            else -> {
                this.nameError = ""
                true
            }
        }
    }

    fun saveGroup(onSuccess: (Group) -> Unit, onError: (String) -> Unit) {
        if (validateName()) {
            _group.update {
                it.copy(
                    name = it.name.trim(),
                    users = it.users + members.associate { member -> member.uuid to GroupUser(id = member.uuid) }
                        .filter { member -> member.value.id !in it.users.keys }
                )
            }
            screenModelScope.launch {
                try {
                    _isLoading.update { true }
                    val savedGroup = groupRepository.saveGroup(
                        _group.value,
                        null
                    )
                    _isLoading.update { false }
                    onSuccess(savedGroup)
                } catch (e: Exception) {
                    logMessage("GroupDetailsViewModel", e.toString())
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun deleteGroup(onDelete: () -> Unit) {
        screenModelScope.launch {
            try {
                _isLoading.update { true }
                groupRepository.deleteGroup(
                    _group.value.id,
                    if (_group.value.image.isNotEmpty()) _group.value.id else ""
                )
                _isLoading.update { false }
                onDelete()
            } catch (e: Exception) {
                logMessage("GroupDetailsViewModel", e.toString())
            }
        }
    }
} 