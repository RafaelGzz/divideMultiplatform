package com.ragl.divide.data.repositories

import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.repositories.FriendsRepository
import com.ragl.divide.presentation.utils.logMessage
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.database.DataSnapshot
import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
class FriendsRepositoryImpl(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) : FriendsRepository {

    private val limit = 8

    private fun snapshotToFriendInfo(snapshot: DataSnapshot): UserInfo {
        val id = snapshot.child("uuid").value<String>()
        val name = snapshot.child("name").value<String>()
        val photoUrl = snapshot.child("photoUrl").value<String>()

        return UserInfo(
            uuid = id,
            name = name,
            photoUrl = photoUrl
        )
    }

    override suspend fun getFriends(friends: List<String>): Map<String, UserInfo> {
        return getUserInfo(friends)
    }

    override suspend fun getGroupMembers(
        userIds: List<String>,
        localUsers: Map<String, UserInfo>
    ): List<UserInfo> {
        val missingMembers = userIds.filter { localUsers[it] == null }
        val localMembers = localUsers.filter { it.key in userIds }.values.toList()

        if (missingMembers.isEmpty()) return localMembers

        logMessage("FriendsRepositoryImpl", "getGroupMembers: $missingMembers")
        val fetchedMembers = getUserInfo(missingMembers)
        val result = localMembers + fetchedMembers.values.toList()

        return result
    }

    suspend fun getUserInfo(userIds: List<String>): Map<String, UserInfo> = coroutineScope {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val deferredResults = userIds.map { userId ->
            async {
                val snapshot = database.reference("users").child(userId).valueEvents.firstOrNull()
                snapshot?.let { snap ->
                    snapshotToFriendInfo(snap)
                }
            }
        }
        
        val results = deferredResults.awaitAll()
        val map = mutableMapOf<String, UserInfo>()
        
        results.filterNotNull().forEach { userInfo ->
            map[userInfo.uuid] = userInfo
        }
        
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("FriendsRepositoryImpl", "getUserInfo: ${map.size} - executed in ${executionTime}ms")
        map
    }

    override suspend fun searchUsers(
        query: String,
        existing: List<UserInfo>
    ): Map<String, UserInfo> = coroutineScope {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val uuid = auth.currentUser!!.uid
        val usersRef =
            database.reference("users").orderByChild("name").startAt(query).limitToFirst(limit)

        val snapshot = usersRef.valueEvents.firstOrNull()
        val children = snapshot?.children?.toList() ?: emptyList()
        
        val deferredResults = children.map { childSnapshot ->
            async {
                snapshotToFriendInfo(childSnapshot)
            }
        }
        
        val results = deferredResults.awaitAll()
        val map = mutableMapOf<String, UserInfo>()
        
        results.forEach { friendInfo ->
            map[friendInfo.uuid] = friendInfo
        }

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("FriendsRepositoryImpl", "searchUsers: ${map.size} - executed in ${executionTime}ms")

        return@coroutineScope map.apply {
            remove(uuid)
            existing.forEach {
                remove(it.uuid)
            }
        }
    }

    override suspend fun addFriend(friend: UserInfo): UserInfo {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val uuid = auth.currentUser!!.uid
        database.reference("users/$uuid/friends/${friend.uuid}").setValue(friend.uuid)
        database.reference("users/${friend.uuid}/friends/${uuid}").setValue(uuid)
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("FriendsRepositoryImpl", "addFriend: $friend - executed in ${executionTime}ms")
        return friend
    }

    override suspend fun sendFriendRequest(friendId: String): Boolean {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val uuid = auth.currentUser!!.uid
        val result = try {
            database.reference("users/$uuid/friendRequestsSent/$friendId").setValue(friendId)
            database.reference("users/$friendId/friendRequestsReceived/$uuid").setValue(uuid)
            true
        } catch (_: Exception) {
            false
        }
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("FriendsRepositoryImpl", "sendFriendRequest: $friendId - executed in ${executionTime}ms")
        return result
    }

    override suspend fun acceptFriendRequest(friendId: String): Boolean {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val uuid = auth.currentUser!!.uid
        val result = try {
            database.reference("users/$uuid/friendRequestsReceived/$friendId").removeValue()
            database.reference("users/$friendId/friendRequestsSent/$uuid").removeValue()

            database.reference("users/$uuid/friends/$friendId").setValue(friendId)
            database.reference("users/$friendId/friends/$uuid").setValue(uuid)
            true
        } catch (_: Exception) {
            false
        }
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("FriendsRepositoryImpl", "acceptFriendRequest: $friendId - executed in ${executionTime}ms")
        return result
    }

    override suspend fun cancelFriendRequest(friendId: String): Boolean {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val uuid = auth.currentUser!!.uid
        val result = try {
            database.reference("users/$uuid/friendRequestsSent/$friendId").removeValue()
            database.reference("users/$friendId/friendRequestsReceived/$uuid").removeValue()
            true
        } catch (_: Exception) {
            false
        }
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("FriendsRepositoryImpl", "cancelFriendRequest: $friendId - executed in ${executionTime}ms")
        return result
    }

    override suspend fun rejectFriendRequest(friendId: String): Boolean {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val uuid = auth.currentUser!!.uid
        val result = try {
            database.reference("users/$uuid/friendRequestsReceived/$friendId").removeValue()
            database.reference("users/$friendId/friendRequestsSent/$uuid").removeValue()
            true
        } catch (_: Exception) {
            false
        }
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("FriendsRepositoryImpl", "rejectFriendRequest: $friendId - executed in ${executionTime}ms")
        return result
    }

    override suspend fun removeFriend(friendId: String): Boolean {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val uuid = auth.currentUser!!.uid
        val result = try {
            database.reference("users/$uuid/friends/$friendId").removeValue()
            database.reference("users/$friendId/friends/$uuid").removeValue()
            true
        } catch (_: Exception) {
            false
        }
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("FriendsRepositoryImpl", "removeFriend: $friendId - executed in ${executionTime}ms")
        return result
    }

    override suspend fun getFriendRequestsReceived(requests: Map<String, String>): Map<String, UserInfo> {
        logMessage("FriendsRepositoryImpl", "getFriendRequestsReceived")
        return getUserInfo(requests.values.toList())
    }

    override suspend fun getFriendRequestsSent(requests: Map<String, String>): Map<String, UserInfo> {
        logMessage("FriendsRepositoryImpl", "getFriendRequestsSent")
        return getUserInfo(requests.values.toList())
    }
}
