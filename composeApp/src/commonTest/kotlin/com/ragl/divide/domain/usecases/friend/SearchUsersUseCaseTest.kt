package com.ragl.divide.domain.usecases.friend

import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.repositories.FriendsRepository
import com.ragl.divide.testing.FakeFriendsRepository
import com.ragl.divide.testing.assertCalled
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchUsersUseCaseTest {

    private val mockFriendsRepository = FakeFriendsRepository()
    private lateinit var useCase: SearchUsersUseCase

    @BeforeTest
    fun setUp() {
        useCase = SearchUsersUseCase(mockFriendsRepository)
    }

    @Test
    fun `should return success with users when search is successful`() = runTest {
        // Given
        val existing = listOf(UserInfo(uuid = "existing1", name="Existing User"))
        val expectedUsers = mapOf(
            "user1" to UserInfo(uuid = "user1", name = "User One"),
            "user2" to UserInfo(uuid = "user2", name = "User Two")
        )

        mockFriendsRepository.onSearchUsers = { _, _ -> expectedUsers }

        // When
        val result = useCase("user", existing)

        // Then
        assertTrue(result is SearchUsersUseCase.Result.Success)
        assertEquals(expectedUsers, result.users)
        assertCalled(mockFriendsRepository, "searchUsers", "user", existing)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val existing = listOf(UserInfo(uuid = "existing1", name="Existing User"))
        val expectedException = Exception("Database error")

        mockFriendsRepository.onSearchUsers = { _, _ -> throw expectedException }

        // When
        val result = useCase("user", existing)

        // Then
        assertTrue(result is SearchUsersUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
    }
}
