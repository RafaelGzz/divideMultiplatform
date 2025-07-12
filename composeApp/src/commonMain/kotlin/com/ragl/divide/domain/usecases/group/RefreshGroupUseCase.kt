package com.ragl.divide.domain.usecases.group

import com.ragl.divide.data.models.Group
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.logMessage

class RefreshGroupUseCase(
    private val groupRepository: GroupRepository,
    private val userStateHolder: UserStateHolder,
    private val strings: Strings
) {
    sealed class Result {
        data class Success(val group: Group) : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(groupId: String): Result {
        return try {
            val group = groupRepository.getGroup(groupId)
            userStateHolder.updateGroupInState(groupId, group)
            Result.Success(group)
        } catch (e: Exception) {
            logMessage("GetGroupUseCase", e.message ?: e.stackTraceToString())
            Result.Error(strings.getUnknownError())
        }
    }
} 