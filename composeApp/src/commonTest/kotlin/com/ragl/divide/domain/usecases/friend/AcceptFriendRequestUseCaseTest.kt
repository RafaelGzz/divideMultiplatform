package com.ragl.divide.domain.usecases.friend

import com.ragl.divide.domain.repositories.FriendsRepository
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AcceptFriendRequestUseCaseTest {

    private val mockFriendsRepository = mock(of<FriendsRepository>())
    private lateinit var useCase: AcceptFriendRequestUseCase

    @BeforeTest
    fun setup() {
        useCase = AcceptFriendRequestUseCase(mockFriendsRepository)
    }

    @Test
    fun `should return success when friend request is accepted successfully`() = runTest {
        // Given
        val friendId = "friend123"
        coEvery { mockFriendsRepository.acceptFriendRequest(friendId) } returns true

        // When
        val result = useCase(friendId)

        // Then
        assertTrue(result is AcceptFriendRequestUseCase.Result.Success)
        assertEquals(true, result.accepted)
        coVerify { mockFriendsRepository.acceptFriendRequest(friendId) }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val friendId = "friend123"
        val expectedException = Exception("Network error")
        coEvery { mockFriendsRepository.acceptFriendRequest(friendId) } throws expectedException

        // When
        val result = useCase(friendId)

        // Then
        assertTrue(result is AcceptFriendRequestUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        coVerify { mockFriendsRepository.acceptFriendRequest(friendId) }
    }
} 