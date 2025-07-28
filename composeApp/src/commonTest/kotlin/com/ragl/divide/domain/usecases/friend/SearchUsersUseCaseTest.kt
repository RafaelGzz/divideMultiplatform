package com.ragl.divide.domain.usecases.friend

import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.repositories.FriendsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchUsersUseCaseTest {

    private val mockFriendsRepository = MockFriendsRepository()
    private val useCase = SearchUsersUseCase(mockFriendsRepository)

    @Test
    fun `should return success with users when search is successful`() = runTest {
        // Given
        val query = "test"
        val existing = listOf(UserInfo(uuid = "existing1", name = "Existing User"))
        val expectedUsers = mapOf(
            "user1" to UserInfo(uuid = "user1", name = "Test User 1"),
            "user2" to UserInfo(uuid = "user2", name = "Test User 2")
        )
        mockFriendsRepository.shouldReturnUsers = expectedUsers

        // When
        val result = useCase(query, existing)

        // Then
        assertTrue(result is SearchUsersUseCase.Result.Success)
        assertEquals(expectedUsers, result.users)
        assertEquals(query, mockFriendsRepository.receivedQuery)
        assertEquals(existing, mockFriendsRepository.receivedExisting)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val query = "test"
        val existing = listOf(UserInfo(uuid = "existing1", name = "Existing User"))
        val expectedException = Exception("Network error")
        mockFriendsRepository.shouldThrowException = expectedException

        // When
        val result = useCase(query, existing)

        // Then
        assertTrue(result is SearchUsersUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
    }

    private class MockFriendsRepository : FriendsRepository {
        var shouldReturnUsers: Map<String, UserInfo> = emptyMap()
        var shouldThrowException: Exception? = null
        var receivedQuery: String? = null
        var receivedExisting: List<UserInfo>? = null

        override suspend fun searchUsers(query: String, existing: List<UserInfo>): Map<String, UserInfo> {
            shouldThrowException?.let { throw it }
            receivedQuery = query
            receivedExisting = existing
            return shouldReturnUsers
        }

        // Implementaciones vacías para otros métodos no utilizados en el test
        override suspend fun getFriends(friends: List<String>): Map<String, UserInfo> = emptyMap()
        override suspend fun getGroupMembers(userIds: List<String>, localUsers: Map<String, UserInfo>): List<UserInfo> = emptyList()
        override suspend fun addFriend(friend: UserInfo): UserInfo = friend
        override suspend fun sendFriendRequest(friendId: String): Boolean = false
        override suspend fun acceptFriendRequest(friendId: String): Boolean = false
        override suspend fun cancelFriendRequest(friendId: String): Boolean = false
        override suspend fun rejectFriendRequest(friendId: String): Boolean = false
        override suspend fun removeFriend(friendId: String): Boolean = false
        override suspend fun getFriendRequestsReceived(requests: Map<String, String>): Map<String, UserInfo> = emptyMap()
        override suspend fun getFriendRequestsSent(requests: Map<String, String>): Map<String, UserInfo> = emptyMap()
    }
} 