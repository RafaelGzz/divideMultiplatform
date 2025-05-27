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
import com.ragl.divide.ui.utils.Strings
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
    private val groupRepository: GroupRepository,
    private val friendsRepository: FriendsRepository,
    private val strings: Strings
) : ScreenModel {

    private var _group = MutableStateFlow(Group())
    var group = _group.asStateFlow()

    // Imagen temporal para mostrar antes de subir a Firebase
    private var _temporaryImagePath = MutableStateFlow<String?>(null)
    val temporaryImagePath = _temporaryImagePath.asStateFlow()

    var members by mutableStateOf<List<UserInfo>>(listOf())
        private set

    var nameError by mutableStateOf("")
        private set

    var simplifyDebts by mutableStateOf(false)
        private set

    private var _isLoading = MutableStateFlow(false)
    var isLoading = _isLoading.asStateFlow()

    // Almacena la ruta del archivo de imagen seleccionado
    private var selectedImagePath: String? = null

    private var currentUserId: String = ""

    fun updateSimplifyDebts(simplify: Boolean) {
        simplifyDebts = simplify
    }

    fun hasUserDebtOrCredit(userId: String = currentUserId): Boolean {
        if (userId.isEmpty()) return false

        val currentDebts = _group.value.currentDebts

        val userDebts = currentDebts[userId]
        if (userDebts != null && userDebts.any { it.value > 0.01 }) {
            return true
        }

        val userCredits = currentDebts.filter { it.key != userId }
            .any { (_, debtMap) ->
                debtMap.containsKey(userId) && debtMap[userId]!! > 0.01
            }

        return userCredits
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

    fun setGroup(group: Group, users: List<UserInfo>) {
        screenModelScope.launch {
            _isLoading.update { true }
            _group.update {
                group
            }
            simplifyDebts = group.simplifyDebts
            members = users
            //friendsRepository.getFriends(_group.value.users.values.toList()).values.toList()
            _isLoading.update { false }
        }
    }

    fun setCurrentUserId(userId: String) {
        currentUserId = userId
    }

    fun leaveGroup(onSuccessful: () -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                if (hasUserDebtOrCredit()) {
                    onError(strings.getDebtOrCreditError())
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

    private fun validateName(): Boolean {
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
                    simplifyDebts = simplifyDebts
                )
            }
            screenModelScope.launch {
                try {
                    _isLoading.update { true }

                    // Crear File de Firebase si hay una imagen seleccionada
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

                    _isLoading.update { false }
                    onSuccess(savedGroup)
                } catch (e: Exception) {
                    _isLoading.update { false }
                    logMessage("GroupDetailsViewModel", e.toString())
                    onError(e.message ?: strings.getUnknownError())
                }
            }
        }
    }

    fun deleteGroup(onDelete: () -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                if (hasUserDebtOrCredit()) {
                    onError(strings.getDebtOrCreditError())
                    return@launch
                }
                _isLoading.update { true }
                groupRepository.deleteGroup(
                    _group.value.id,
                    if (_group.value.image.isNotEmpty()) _group.value.id else ""
                )
                _isLoading.update { false }
                onDelete()
            } catch (e: Exception) {
                logMessage("GroupDetailsViewModel", e.toString())
                onError(e.message ?: strings.getUnknownError())
            }
        }
    }
} 