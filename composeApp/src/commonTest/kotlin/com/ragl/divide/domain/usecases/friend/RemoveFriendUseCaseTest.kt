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

class RemoveFriendUseCaseTest {

    private val mockFriendsRepository = mock(of<FriendsRepository>())
    private lateinit var useCase: RemoveFriendUseCase

    @BeforeTest
    fun setUp() {
        useCase = RemoveFriendUseCase(mockFriendsRepository)
    }

    @Test
    fun `should return success when friend is removed successfully`() = runTest {
        // Given
        val userId = "user123"

        coEvery { mockFriendsRepository.removeFriend( userId) } returns true

        // When
        val result = useCase( userId)

        // Then
        assertTrue(result is RemoveFriendUseCase.Result.Success)
        coVerify { mockFriendsRepository.removeFriend( userId) }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val userId = "user123"
        val expectedException = Exception("Database error")

        coEvery { mockFriendsRepository.removeFriend( userId) } throws expectedException

        // When
        val result = useCase( userId)

        // Then
        assertTrue(result is RemoveFriendUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
    }
}
