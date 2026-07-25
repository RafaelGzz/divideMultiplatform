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

class SendFriendRequestUseCaseTest {

    private val mockFriendsRepository = mock(of<FriendsRepository>())
    private lateinit var useCase: SendFriendRequestUseCase

    @BeforeTest
    fun setup() {
        useCase = SendFriendRequestUseCase(mockFriendsRepository)
    }

    @Test
    fun `should return success when friend request is sent successfully`() = runTest {
        // Given
        val friendId = "friend123"
        coEvery { mockFriendsRepository.sendFriendRequest(friendId) } returns true

        // When
        val result = useCase(friendId)

        // Then
        assertTrue(result is SendFriendRequestUseCase.Result.Success)
        assertEquals(true, result.sent)
        coVerify { mockFriendsRepository.sendFriendRequest(friendId) }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val friendId = "friend123"
        val expectedException = Exception("Network error")
        coEvery { mockFriendsRepository.sendFriendRequest(friendId) } throws expectedException

        // When
        val result = useCase(friendId)

        // Then
        assertTrue(result is SendFriendRequestUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        coVerify { mockFriendsRepository.sendFriendRequest(friendId) }
    }
} 