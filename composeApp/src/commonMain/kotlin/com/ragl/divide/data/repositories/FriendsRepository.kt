package com.ragl.divide.data.repositories

import com.ragl.divide.data.models.UserInfo
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.database.DataSnapshot
import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.firstOrNull

interface FriendsRepository {
    suspend fun getFriends(friends: List<String>): Map<String, UserInfo>
    suspend fun searchUsers(query: String, existing: List<UserInfo>): Map<String, UserInfo>
    suspend fun addFriend(friend: UserInfo): UserInfo
    suspend fun sendFriendRequest(friendId: String): Boolean
    suspend fun acceptFriendRequest(friendId: String): Boolean
    suspend fun cancelFriendRequest(friendId: String): Boolean
    suspend fun rejectFriendRequest(friendId: String): Boolean
    suspend fun getFriendRequestsReceived(requests: Map<String, String>): Map<String, UserInfo>
    suspend fun getFriendRequestsSent(requests: Map<String, String>): Map<String, UserInfo>
}

class FriendsRepositoryImpl(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) : FriendsRepository{

    private val limit = 8
    
    // Función auxiliar para convertir DataSnapshot a FriendInfo
    private fun snapshotToFriendInfo(snapshot: DataSnapshot): UserInfo? {
        val id = snapshot.child("uuid").value<String>() ?: return null
        val name = snapshot.child("name").value<String>() ?: return null
        val photoUrl = snapshot.child("photoUrl").value<String>() ?: ""
        
        return UserInfo(
            uuid = id,
            name = name,
            photoUrl = photoUrl
        )
    }

    override suspend fun getFriends(friends: List<String>): Map<String, UserInfo> {
        val map = mutableMapOf<String, UserInfo>()
        friends.forEach {
            val snapshot = database.reference("users").child(it).valueEvents.firstOrNull()
            
            // Convertir directamente a FriendInfo
            snapshot?.let { snap ->
                snapshotToFriendInfo(snap)?.let { friendInfo ->
                    map[friendInfo.uuid] = friendInfo
                }
            }
        }
        return map
    }

    override suspend fun searchUsers(query: String, existing: List<UserInfo>): Map<String, UserInfo> {
        val uuid = auth.currentUser!!.uid
        val map = mutableMapOf<String, UserInfo>()
        val usersRef =
            database.reference("users").orderByChild("name").startAt(query).limitToFirst(limit)

        // Convertir directamente a FriendInfo
        usersRef.valueEvents.firstOrNull()?.children?.forEach { snapshot ->
            snapshotToFriendInfo(snapshot)?.let { friendInfo ->
                map[friendInfo.uuid] = friendInfo
            }
        }
        
        return map.apply {
            remove(uuid)
            existing.forEach {
                remove(it.uuid)
            }
        }
    }

    override suspend fun addFriend(friend: UserInfo): UserInfo {
        val uuid = auth.currentUser!!.uid
        database.reference("users/$uuid/friends/${friend.uuid}").setValue(friend.uuid)
        database.reference("users/${friend.uuid}/friends/${uuid}").setValue(uuid)
        return friend
    }

    override suspend fun sendFriendRequest(friendId: String): Boolean {
        val uuid = auth.currentUser!!.uid
        try {
            // Añadir solicitud a la lista de enviadas del usuario actual
            database.reference("users/$uuid/friendRequestsSent/$friendId").setValue(friendId)
            // Añadir solicitud a la lista de recibidas del amigo
            database.reference("users/$friendId/friendRequestsReceived/$uuid").setValue(uuid)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override suspend fun acceptFriendRequest(friendId: String): Boolean {
        val uuid = auth.currentUser!!.uid
        try {
            // Eliminar solicitud de la lista de recibidas del usuario
            database.reference("users/$uuid/friendRequestsReceived/$friendId").removeValue()
            // Eliminar solicitud de la lista de enviadas del amigo
            database.reference("users/$friendId/friendRequestsSent/$uuid").removeValue()
            
            // Añadir como amigos mutuamente
            database.reference("users/$uuid/friends/$friendId").setValue(friendId)
            database.reference("users/$friendId/friends/$uuid").setValue(uuid)
            
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override suspend fun cancelFriendRequest(friendId: String): Boolean {
        val uuid = auth.currentUser!!.uid
        try {
            // Eliminar solicitud de la lista de enviadas del usuario
            database.reference("users/$uuid/friendRequestsSent/$friendId").removeValue()
            // Eliminar solicitud de la lista de recibidas del amigo
            database.reference("users/$friendId/friendRequestsReceived/$uuid").removeValue()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override suspend fun rejectFriendRequest(friendId: String): Boolean {
        val uuid = auth.currentUser!!.uid
        try {
            // Eliminar solicitud de la lista de recibidas del usuario
            database.reference("users/$uuid/friendRequestsReceived/$friendId").removeValue()
            // Eliminar solicitud de la lista de enviadas del amigo
            database.reference("users/$friendId/friendRequestsSent/$uuid").removeValue()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override suspend fun getFriendRequestsReceived(requests: Map<String, String>): Map<String, UserInfo> {
        val map = mutableMapOf<String, UserInfo>()
        requests.forEach {
            val snapshot = database.reference("users").child(it.value).valueEvents.firstOrNull()
            
            // Convertir directamente a FriendInfo
            snapshot?.let { snap ->
                snapshotToFriendInfo(snap)?.let { friendInfo ->
                    map[friendInfo.uuid] = friendInfo
                }
            }
        }
        return map
    }

    override suspend fun getFriendRequestsSent(requests: Map<String, String>): Map<String, UserInfo> {
        val map = mutableMapOf<String, UserInfo>()
        requests.forEach {
            val snapshot = database.reference("users").child(it.value).valueEvents.firstOrNull()
            
            // Convertir directamente a FriendInfo
            snapshot?.let { snap ->
                snapshotToFriendInfo(snap)?.let { friendInfo ->
                    map[friendInfo.uuid] = friendInfo
                }
            }
        }
        return map
    }
}