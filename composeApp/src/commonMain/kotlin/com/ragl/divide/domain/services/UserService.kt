package com.ragl.divide.domain.services

import com.ragl.divide.data.models.UserInfo

interface UserService {
    suspend fun isFirstTime(): Boolean
    fun completeOnboarding()
    fun changeDarkMode(isDarkMode: Boolean?)
    fun signOut(onSignOut: () -> Unit)
    fun updateProfileImage(imagePath: String, onSuccess: () -> Unit, onError: (String) -> Unit)
    fun updateUserName(newName: String, onSuccess: () -> Unit, onError: (String) -> Unit)
    fun sendFriendRequest(friend: UserInfo)
    fun acceptFriendRequest(friend: UserInfo)
    fun rejectFriendRequest(friend: UserInfo)
    fun cancelFriendRequest(friend: UserInfo)
    fun removeFriend(friend: UserInfo)
} 