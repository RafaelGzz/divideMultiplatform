package com.ragl.divide.domain.usecases.friend

import com.ragl.divide.domain.repositories.FriendsRepository
import com.ragl.divide.testing.FakeFriendsRepository
import com.ragl.divide.testing.assertCalled
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AcceptFriendRequestUseCaseTest {

    private val mockFriendsRepository = FakeFriendsRepository()
    private lateinit var useCase: AcceptFriendRequestUseCase

    @BeforeTest
    fun setup() {
        useCase = AcceptFriendRequestUseCase(mockFriendsRepository)
    }

    @Test
    fun `should return success when friend request is accepted successfully`() = runTest {
        // Given
        val friendId = "friend123"
        mockFriendsRepository.onAcceptFriendRequest = { true }

        // When
        val result = useCase(friendId)

        // Then
        assertTrue(result is AcceptFriendRequestUseCase.Result.Success)
        assertEquals(true, result.accepted)
        assertCalled(mockFriendsRepository, "acceptFriendRequest", friendId)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val friendId = "friend123"
        val expectedException = Exception("Network error")
        mockFriendsRepository.onAcceptFriendRequest = { throw expectedException }

        // When
        val result = useCase(friendId)

        // Then
        assertTrue(result is AcceptFriendRequestUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        assertCalled(mockFriendsRepository, "acceptFriendRequest", friendId)
    }
} 