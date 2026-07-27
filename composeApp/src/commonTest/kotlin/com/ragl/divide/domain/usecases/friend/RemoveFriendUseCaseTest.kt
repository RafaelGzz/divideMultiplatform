package com.ragl.divide.domain.usecases.friend

import com.ragl.divide.domain.repositories.FriendsRepository
import com.ragl.divide.testing.FakeFriendsRepository
import com.ragl.divide.testing.assertCalled
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RemoveFriendUseCaseTest {

    private val mockFriendsRepository = FakeFriendsRepository()
    private lateinit var useCase: RemoveFriendUseCase

    @BeforeTest
    fun setUp() {
        useCase = RemoveFriendUseCase(mockFriendsRepository)
    }

    @Test
    fun `should return success when friend is removed successfully`() = runTest {
        // Given
        val userId = "user123"

        mockFriendsRepository.onRemoveFriend = { true }

        // When
        val result = useCase( userId)

        // Then
        assertTrue(result is RemoveFriendUseCase.Result.Success)
        assertCalled(mockFriendsRepository, "removeFriend", userId)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val userId = "user123"
        val expectedException = Exception("Database error")

        mockFriendsRepository.onRemoveFriend = { throw expectedException }

        // When
        val result = useCase( userId)

        // Then
        assertTrue(result is RemoveFriendUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
    }
}
