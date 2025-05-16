package com.ragl.divide.data.repositories

import com.ragl.divide.data.models.User
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.firstOrNull

interface FriendsRepository {
    suspend fun getFriends(friends: Map<String, String>): Map<String, User>
    suspend fun searchUsers(query: String, existing: List<User>): Map<String, User>
    suspend fun addFriend(friend: User): User
}

class FriendsRepositoryImpl(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) : FriendsRepository{

    private val limit = 8

    override suspend fun getFriends(friends: Map<String, String>): Map<String, User> {
        val map = mutableMapOf<String, User>()
        friends.forEach {
            database.reference("users").child(it.value).valueEvents.firstOrNull()?.value<User>()
                ?.let { user -> map[user.uuid] = user }
        }
        return map
    }

    override suspend fun searchUsers(query: String, existing: List<User>): Map<String, User> {
        val uuid = auth.currentUser!!.uid
        val map = mutableMapOf<String, User>()
        val usersRef =
            database.reference("users").orderByChild("name").startAt(query).limitToFirst(limit)

        // Use firstOrNull to get just the first emission from the flow
        usersRef.valueEvents.firstOrNull()?.children?.forEach {
            it.value<User>().let { user -> map[user.uuid] = user }
        }
        
        return map.apply {
            remove(uuid)
            existing.forEach {
                remove(it.uuid)
            }
        }
    }

    override suspend fun addFriend(friend: User): User {
        val uuid = auth.currentUser!!.uid
        database.reference("users/$uuid/friends/${friend.uuid}").setValue(friend.uuid)
        database.reference("users/${friend.uuid}/friends/${uuid}").setValue(uuid)
        return friend
    }

}