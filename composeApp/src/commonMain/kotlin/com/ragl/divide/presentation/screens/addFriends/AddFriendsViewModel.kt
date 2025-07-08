package com.ragl.divide.presentation.screens.addFriends

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.repositories.FriendsRepository
import com.ragl.divide.domain.services.UserService
import kotlinx.coroutines.launch

class AddFriendsViewModel(
    private val repository: FriendsRepository,
    private val userService: UserService
) : ScreenModel {

    var isLoading by mutableStateOf(false)
        private set

    var searchText by mutableStateOf("")
        private set

    var users by mutableStateOf(emptyMap<String, UserInfo>())
        private set

    var selectedUser by mutableStateOf<UserInfo?>(null)
        private set

    private var friends by mutableStateOf(emptyList<UserInfo>())
    private var friendRequestsSent by mutableStateOf(emptyList<UserInfo>())

    fun updateCurrentFriends(currentFriends: List<UserInfo>) {
        friends = currentFriends
    }
    
    fun updateFriendRequestsSent(requests: List<UserInfo>) {
        friendRequestsSent = requests
    }

    fun updateSelectedUser(user: UserInfo) {
        selectedUser = user
    }

    fun updateSearchText(text: String) {
        searchText = text
    }

    fun searchUser() {
        if (searchText.isEmpty()) {
            users = emptyMap()
            return
        }
        screenModelScope.launch {
            isLoading = true
            val existingUsers = friends + friendRequestsSent
            users = repository.searchUsers(searchText, existingUsers)
            isLoading = false
        }
    }

    fun sendFriendRequest() {
        screenModelScope.launch {
            try {
                isLoading = true
                if (repository.sendFriendRequest(selectedUser!!.uuid)) {
                    updateFriendRequestsSent(friendRequestsSent + selectedUser!!)
                    searchUser()
                    userService.sendFriendRequest(selectedUser!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}