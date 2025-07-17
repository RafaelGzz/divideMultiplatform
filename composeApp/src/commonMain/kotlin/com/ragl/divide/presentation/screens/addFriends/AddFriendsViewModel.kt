package com.ragl.divide.presentation.screens.addFriends

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.services.AppStateService
import com.ragl.divide.domain.services.UserService
import com.ragl.divide.domain.usecases.friend.SearchUsersUseCase
import com.ragl.divide.domain.usecases.friend.SendFriendRequestUseCase
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.logMessage
import kotlinx.coroutines.launch

class AddFriendsViewModel(
    private val searchUsersUseCase: SearchUsersUseCase,
    private val sendFriendRequestUseCase: SendFriendRequestUseCase,
    private val userService: UserService,
    private val appStateService: AppStateService,
    private val strings: Strings
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
            when (val result = searchUsersUseCase(searchText, existingUsers)) {
                is SearchUsersUseCase.Result.Success -> {
                    users = result.users
                }
                is SearchUsersUseCase.Result.Error -> {
                    logMessage("SearchUsersUseCase", result.exception.message ?: result.exception.stackTraceToString())
                    appStateService.handleError(strings.getUnknownError())
                }
            }
            isLoading = false
        }
    }

    fun sendFriendRequest() {
        screenModelScope.launch {
            try {
                isLoading = true
                when (val result = sendFriendRequestUseCase(selectedUser!!.uuid)) {
                    is SendFriendRequestUseCase.Result.Success -> {
                        updateFriendRequestsSent(friendRequestsSent + selectedUser!!)
                        searchUser()
                        userService.sendFriendRequest(selectedUser!!)
                    }
                    is SendFriendRequestUseCase.Result.Error -> {
                        logMessage("SendFriendRequestUseCase", result.exception.message ?: result.exception.stackTraceToString())
                        appStateService.handleError(strings.getUnknownError())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}