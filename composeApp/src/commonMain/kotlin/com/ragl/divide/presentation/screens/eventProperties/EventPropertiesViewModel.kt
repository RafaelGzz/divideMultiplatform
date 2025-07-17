package com.ragl.divide.presentation.screens.eventProperties

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Category
import com.ragl.divide.data.models.Event
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.domain.usecases.event.DeleteEventUseCase
import com.ragl.divide.domain.usecases.event.SaveEventUseCase
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.logMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventPropertiesViewModel(
    private val saveEventUseCase: SaveEventUseCase,
    private val deleteEventUseCase: DeleteEventUseCase,
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
        screenModelScope.launch {
            if (validateTitle().and(validateDescription())) {
                _event.update { it.copy(description = it.description.trim()) }
                when (val result = saveEventUseCase(groupId, _event.value)) {
                    is SaveEventUseCase.Result.Success -> {
                        onSuccess()
                    }

                                    is SaveEventUseCase.Result.Error -> {
                    logMessage("SaveEventUseCase", result.exception.message ?: result.exception.stackTraceToString())
                    onError(strings.getUnknownError())
                }
                }
            }
        }
    }

    fun deleteEvent(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!canDeleteGroup) {
            onError(strings.getCannotDeleteEvent())
            return
        }
        screenModelScope.launch {
            when (val result = deleteEventUseCase(groupId, _event.value.id)) {
                is DeleteEventUseCase.Result.Success -> {
                    onSuccess()
                }

                is DeleteEventUseCase.Result.Error -> {
                    logMessage("DeleteEventUseCase", result.exception.message ?: result.exception.stackTraceToString())
                    onError(strings.getUnknownError())
                }
            }
        }
    }
} 