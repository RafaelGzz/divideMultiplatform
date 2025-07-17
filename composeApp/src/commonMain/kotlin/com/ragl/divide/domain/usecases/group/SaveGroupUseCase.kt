package com.ragl.divide.domain.usecases.group

import com.ragl.divide.data.models.Group
import com.ragl.divide.domain.repositories.FriendsRepository
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import dev.gitlive.firebase.storage.File

class SaveGroupUseCase(
    private val groupRepository: GroupRepository,
    private val friendsRepository: FriendsRepository,
    private val userStateHolder: UserStateHolder,
) {
    sealed class Result {
        data object Success : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(group: Group, imageFile: File?): Result {
        return try {
            val savedGroup = groupRepository.saveGroup(group, imageFile)

            val currentGroupMembers = userStateHolder.getGroupMembers(group.id)
            val currentGroupMembersIds = currentGroupMembers.map { it.uuid }
            val newGroupUserIds = savedGroup.users.values.toList()

            val missingUserIds = newGroupUserIds.filter { it !in currentGroupMembersIds }
            if (missingUserIds.isNotEmpty()) {
                val newGroupMembers = friendsRepository.getFriends(missingUserIds)
                val updatedGroupMembers = currentGroupMembers + newGroupMembers.values

                userStateHolder.setGroupMembers(savedGroup, updatedGroupMembers)
            }
            userStateHolder.saveGroup(savedGroup)
            Result.Success
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 