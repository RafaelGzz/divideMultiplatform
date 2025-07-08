package com.ragl.divide.presentation.screens.eventProperties

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Category
import com.ragl.divide.data.models.Event
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.utils.Strings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventPropertiesViewModel(
    private val groupRepository: GroupRepository,
    private val userStateHolder: UserStateHolder,
    private val strings: Strings
) : ScreenModel {

    private val _event = MutableStateFlow(Event())
    val event: StateFlow<Event> = _event.asStateFlow()

    var titleError by mutableStateOf<String?>(null)
    var descriptionError by mutableStateOf<String?>(null)

    val descriptionCharacterLimit = 100

    var canDeleteGroup by mutableStateOf(false)
    val isUpdate = MutableStateFlow(false)

    var groupId by mutableStateOf("")
        private set

    private fun canDeleteGroup(): Boolean {
        return _event.value.currentDebts.isEmpty()
    }

    fun setEvent(groupId: String, eventId: String?) {
        val event = userStateHolder.getEventById(groupId, eventId)

        this.groupId = groupId
        _event.value = event
        isUpdate.value = event.id.isNotEmpty()
        canDeleteGroup = canDeleteGroup()
    }

    fun updateTitle(title: String) {
        _event.value = _event.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _event.value = _event.value.copy(description = description)
    }

    fun updateCategory(category: Category) {
        _event.value = _event.value.copy(category = category)
    }

    fun validateTitle(): Boolean {
        if (_event.value.title.isEmpty()) {
            titleError = strings.getTitleRequired()
            return false
        } else {
            return true
        }
    }

    fun validateDescription(): Boolean {
        if (_event.value.description.trim().length > descriptionCharacterLimit) {
            descriptionError = strings.getDescriptionTooLong()
            return false
        } else {
            descriptionError = ""
            return true
        }
    }

    fun saveEvent(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (validateTitle().and(validateDescription())) {
            try {
                screenModelScope.launch {
                    _event.update {
                        it.copy(description = it.description.trim())
                    }
                    val savedEvent = groupRepository.saveEvent(groupId, _event.value)
                    userStateHolder.saveEvent(groupId, savedEvent)
                    onSuccess()
                }
            } catch (e: Exception) {
                onError(e.message.toString())
            }
        }
    }

    fun deleteEvent(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            if (!canDeleteGroup) {
                onError(strings.getCannotDeleteEvent())
                return
            }
            screenModelScope.launch {
                groupRepository.deleteEvent(groupId, _event.value.id)
                userStateHolder.deleteEvent(groupId, _event.value.id)
                onSuccess()
            }
        } catch (e: Exception) {
            onError(e.message.toString())
        }
    }
} 