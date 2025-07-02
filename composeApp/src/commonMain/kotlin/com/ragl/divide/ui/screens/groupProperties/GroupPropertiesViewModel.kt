package com.ragl.divide.ui.screens.groupProperties

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.data.repositories.FriendsRepository
import com.ragl.divide.data.repositories.GroupRepository
import com.ragl.divide.data.services.GroupExpenseService
import com.ragl.divide.ui.utils.Strings
import com.ragl.divide.ui.utils.logMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * ViewModel para la pantalla de propiedades del grupo, enfocado en la edición de información
 * del grupo como nombre, descripción, imagen, y gestión de miembros.
 */
class GroupPropertiesViewModel(
    private val groupRepository: GroupRepository,
    private val friendsRepository: FriendsRepository,
    private val groupExpenseService: GroupExpenseService,
    private val strings: Strings
) : ScreenModel {

    private var _group = MutableStateFlow(Group())
    var group = _group.asStateFlow()

    private var _temporaryImagePath = MutableStateFlow<String?>(null)
    val temporaryImagePath = _temporaryImagePath.asStateFlow()

    var members by mutableStateOf<List<UserInfo>>(listOf())
        private set

    var guests by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    var nameError by mutableStateOf("")
        private set

    var guestNameError by mutableStateOf("")

    var simplifyDebts by mutableStateOf(true)
        private set

    private var selectedImagePath: String? = null

    private var currentUserId: String = ""

    var canLeaveGroup by mutableStateOf(false)
    var canDeleteGroup by mutableStateOf(false)

    fun updateSimplifyDebts(simplify: Boolean) {
        simplifyDebts = simplify
    }

    private fun canLeaveGroup(): Boolean {
        if (currentUserId.isEmpty()) return false

        // Consolidar deudas de todos los eventos usando el servicio
        val consolidatedDebts = groupExpenseService.consolidateDebtsFromEventsMap(_group.value.events)

        val userDebts = consolidatedDebts[currentUserId]
        if (userDebts != null) {
            return false
        }

        val userCredits = consolidatedDebts.filter { it.key != currentUserId }
            .any { (_, debtMap) ->
                debtMap.containsKey(currentUserId)
            }

        return !userCredits
    }

    fun canDeleteGroup(): Boolean {
        // Consolidar deudas de todos los eventos usando el servicio
        val consolidatedDebts = groupExpenseService.consolidateDebtsFromEventsMap(_group.value.events)
        
        return consolidatedDebts.isEmpty()
    }

    fun updateName(name: String) {
        _group.update {
            it.copy(name = name)
        }
    }

    fun updateImage(imagePath: String) {
        selectedImagePath = imagePath
        _temporaryImagePath.update { imagePath }
    }

    fun addMember(member: UserInfo) {
        this.members += member
    }

    fun removeMember(member: UserInfo) {
        this.members -= member
    }

    fun addGuest(guestName: String): Boolean {
        if (validateGuestName(guestName)) {
            val guestId = "guest_${Clock.System.now().toEpochMilliseconds()}"
            guests = guests + (guestId to guestName)
            return true
        }
        return false
    }

    fun updateGuestName(guestId: String, newName: String): Boolean {
        if (validateGuestName(newName, excludeGuestId = guestId)) {
            guests = guests.toMutableMap().apply {
                this[guestId] = newName
            }
            guestNameError = ""
            return true
        }
        return false
    }

    fun removeGuest(guestId: String) {
        guests = guests - guestId
    }

    fun canRemoveGuest(guestId: String): Boolean {
        val totalOtherMembers = members.size + guests.size - 1
        if (totalOtherMembers == 1) {
            return false
        }
        
        val group = _group.value
        
        val hasDebtsInEvents = group.events.values.any { event ->
            val hasDebtsInEventExpenses = event.expenses.values.any { expense ->
                expense.payers.containsKey(guestId) || expense.debtors.containsKey(guestId)
            }
            
            val hasDebtsInEventPayments = event.payments.values.any { payment ->
                payment.from == guestId || payment.to == guestId
            }
            
            val hasCurrentDebts = event.currentDebts.containsKey(guestId) ||
                                 event.currentDebts.values.any { it.containsKey(guestId) }
            
            hasDebtsInEventExpenses || hasDebtsInEventPayments || hasCurrentDebts
        }
        
        return !hasDebtsInEvents
    }

    fun getRemoveGuestErrorMessage(): String {
        val totalOtherMembers = members.size + guests.size - 1
        return if (totalOtherMembers == 1) {
            strings.getCannotRemoveLastMember()
        } else {
            strings.getCannotRemoveGuestWithDebts()
        }
    }

    private fun validateGuestName(name: String, excludeGuestId: String? = null): Boolean {
        guestNameError = when {
            name.isBlank() -> strings.getNameRequired()
            name.length > 20 -> strings.getNameTooLong()
            name.contains(" ") -> strings.getNameCannotContainSpaces()
            isNameAlreadyUsed(name, excludeGuestId) -> strings.getNameAlreadyExists()
            else -> {
                ""
            }
        }
        return guestNameError.isEmpty()
    }

    private fun isNameAlreadyUsed(name: String, excludeGuestId: String?): Boolean {
        // Verificar contra nombres de miembros
        val memberNames = members.map { it.name }
        if (memberNames.contains(name)) return true
        
        // Verificar contra otros invitados
        val otherGuestNames = guests.filterKeys { it != excludeGuestId }.values
        return otherGuestNames.contains(name)
    }

    fun setGroup(group: Group, users: List<UserInfo>, userId: String) {
        screenModelScope.launch {
            currentUserId = userId
            _group.update {
                group
            }
            simplifyDebts = group.simplifyDebts
            members = users
            guests = group.guests
            canLeaveGroup = canLeaveGroup()
            canDeleteGroup = canDeleteGroup()
        }
    }

    fun leaveGroup(onSuccessful: () -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                if (!canLeaveGroup()) {
                    onError(strings.getCannotLeaveGroup())
                    return@launch
                }
                groupRepository.leaveGroup(_group.value.id)
                onSuccessful()
            } catch (e: Exception) {
                logMessage("GroupDetailsViewModel", e.toString())
                onError(e.message ?: strings.getUnknownError())
            }
        }
    }

    fun validateName(): Boolean {
        return when (_group.value.name) {
            "" -> {
                this.nameError = strings.getTitleRequired()
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
                    users = members.associate { member -> member.uuid to member.uuid },
                    guests = guests,
                    simplifyDebts = simplifyDebts
                )
            }
            screenModelScope.launch {
                try {
                    val imageFile = selectedImagePath?.let { path ->
                        PlatformImageUtils.createFirebaseFile(path)
                    }
                    val savedGroup = groupRepository.saveGroup(
                        _group.value,
                        imageFile
                    )
                    // Limpiar imagen temporal después de guardar
                    _temporaryImagePath.update { null }
                    selectedImagePath = null

                    onSuccess(savedGroup)
                } catch (e: Exception) {
                    logMessage("GroupDetailsViewModel", e.toString())
                    onError(e.message ?: strings.getUnknownError())
                }
            }
        }
    }

    fun deleteGroup(onSuccess: () -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                if (!canDeleteGroup()) {
                    onError(strings.getCannotDeleteGroup())
                    return@launch
                }
                groupRepository.deleteGroup(
                    _group.value.id,
                    if (_group.value.image.isNotEmpty()) _group.value.id else ""
                )
                onSuccess()
            } catch (e: Exception) {
                logMessage("GroupDetailsViewModel", e.toString())
                onError(e.message ?: strings.getUnknownError())
            }
        }
    }
} 