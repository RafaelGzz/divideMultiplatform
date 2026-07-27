package com.ragl.divide.domain.usecases.friend

import com.ragl.divide.domain.repositories.FriendsRepository
import com.ragl.divide.testing.FakeFriendsRepository
import com.ragl.divide.testing.assertCalled
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SendFriendRequestUseCaseTest {

    private val mockFriendsRepository = FakeFriendsRepository()
    private lateinit var useCase: SendFriendRequestUseCase

    @BeforeTest
    fun setup() {
        useCase = SendFriendRequestUseCase(mockFriendsRepository)
    }

    @Test
    fun `should return success when friend request is sent successfully`() = runTest {
        // Given
        val friendId = "friend123"
        mockFriendsRepository.onSendFriendRequest = { true }

        // When
        val result = useCase(friendId)

        // Then
        assertTrue(result is SendFriendRequestUseCase.Result.Success)
        assertEquals(true, result.sent)
        assertCalled(mockFriendsRepository, "sendFriendRequest", friendId)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val friendId = "friend123"
        val expectedException = Exception("Network error")
        mockFriendsRepository.onSendFriendRequest = { throw expectedException }

        // When
        val result = useCase(friendId)

        // Then
        assertTrue(result is SendFriendRequestUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        assertCalled(mockFriendsRepository, "sendFriendRequest", friendId)
    }
} 