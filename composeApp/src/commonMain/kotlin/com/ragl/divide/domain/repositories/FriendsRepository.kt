package com.ragl.divide.domain.repositories

import com.ragl.divide.data.models.UserInfo

interface FriendsRepository {
    suspend fun getFriends(friends: List<String>): Map<String, UserInfo>
    suspend fun getGroupMembers(userIds: List<String>, localUsers: Map<String, UserInfo>): List<UserInfo>
    suspend fun searchUsers(query: String, existing: List<UserInfo>): Map<String, UserInfo>
    suspend fun addFriend(friend: UserInfo): UserInfo
    suspend fun sendFriendRequest(friendId: String): Boolean
    suspend fun acceptFriendRequest(friendId: String): Boolean
    suspend fun cancelFriendRequest(friendId: String): Boolean
    suspend fun rejectFriendRequest(friendId: String): Boolean
    suspend fun removeFriend(friendId: String): Boolean
    suspend fun getFriendRequestsReceived(requests: Map<String, String>): Map<String, UserInfo>
    suspend fun getFriendRequestsSent(requests: Map<String, String>): Map<String, UserInfo>
}