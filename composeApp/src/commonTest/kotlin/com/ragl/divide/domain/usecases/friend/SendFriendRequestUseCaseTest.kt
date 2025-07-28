package com.ragl.divide.domain.usecases.friend

import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.repositories.FriendsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SendFriendRequestUseCaseTest {

    private val mockFriendsRepository = MockFriendsRepository()
    private val useCase = SendFriendRequestUseCase(mockFriendsRepository)

    @Test
    fun `should return success when friend request is sent successfully`() = runTest {
        // Given
        val friendId = "friend123"
        mockFriendsRepository.shouldReturnSent = true

        // When
        val result = useCase(friendId)

        // Then
        assertTrue(result is SendFriendRequestUseCase.Result.Success)
        assertEquals(true, result.sent)
        assertEquals(friendId, mockFriendsRepository.receivedFriendId)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val friendId = "friend123"
        val expectedException = Exception("Network error")
        mockFriendsRepository.shouldThrowException = expectedException

        // When
        val result = useCase(friendId)

        // Then
        assertTrue(result is SendFriendRequestUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
    }

    private class MockFriendsRepository : FriendsRepository {
        var shouldReturnSent: Boolean = false
        var shouldThrowException: Exception? = null
        var receivedFriendId: String? = null

        override suspend fun sendFriendRequest(friendId: String): Boolean {
            shouldThrowException?.let { throw it }
            receivedFriendId = friendId
            return shouldReturnSent
        }

        // Implementaciones vacías para otros métodos no utilizados en el test
        override suspend fun getFriends(friends: List<String>): Map<String, UserInfo> = emptyMap()
        override suspend fun getGroupMembers(userIds: List<String>, localUsers: Map<String, UserInfo>): List<UserInfo> = emptyList()
        override suspend fun searchUsers(query: String, existing: List<UserInfo>): Map<String, UserInfo> = emptyMap()
        override suspend fun addFriend(friend: UserInfo): UserInfo = friend
        override suspend fun acceptFriendRequest(friendId: String): Boolean = false
        override suspend fun cancelFriendRequest(friendId: String): Boolean = false
        override suspend fun rejectFriendRequest(friendId: String): Boolean = false
        override suspend fun removeFriend(friendId: String): Boolean = false
        override suspend fun getFriendRequestsReceived(requests: Map<String, String>): Map<String, UserInfo> = emptyMap()
        override suspend fun getFriendRequestsSent(requests: Map<String, String>): Map<String, UserInfo> = emptyMap()
    }
} 