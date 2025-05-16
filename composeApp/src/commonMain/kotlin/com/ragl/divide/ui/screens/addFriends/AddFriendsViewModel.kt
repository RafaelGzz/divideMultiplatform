package com.ragl.divide.ui.screens.addFriends

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.User
import com.ragl.divide.data.repositories.FriendsRepository
import kotlinx.coroutines.launch

class AddFriendsViewModel(
    private val repository: FriendsRepository
) : ScreenModel {

    var isLoading by mutableStateOf(false)
        private set

    var searchText by mutableStateOf("")
        private set

    var users by mutableStateOf(emptyMap<String, User>())
        private set

    var selectedUser by mutableStateOf<User?>(null)
        private set

    private var friends by mutableStateOf(emptyList<User>())

    fun setCurrentFriends(friends: List<User>) {
        this.friends = friends
    }

    fun updateSelectedUser(user: User) {
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
            users = repository.searchUsers(searchText, friends)
            isLoading = false
        }
    }

    fun addFriend(onFriendAdded: (User) -> Unit) {
        screenModelScope.launch {
            try {
                repository.addFriend(selectedUser!!)
                setCurrentFriends(friends + selectedUser!!)
                searchUser()
                onFriendAdded(selectedUser!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}