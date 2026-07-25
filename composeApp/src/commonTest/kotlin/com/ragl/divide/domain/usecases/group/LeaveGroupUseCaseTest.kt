package com.ragl.divide.domain.usecases.group

import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import io.mockative.any
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaveGroupUseCaseTest {

    private val mockGroupRepository = mock(of<GroupRepository>())
    private val mockUserStateHolder = mock(of<UserStateHolder>())
    private lateinit var useCase: LeaveGroupUseCase

    @BeforeTest
    fun setUp() {
        useCase = LeaveGroupUseCase(mockGroupRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when group is left successfully`() = runTest {
        // Given
        val groupId = "group123"

        coEvery { mockGroupRepository.leaveGroup(groupId) } returns Unit
        coEvery { mockUserStateHolder.deleteGroup(groupId) } returns Unit

        // When
        val result = useCase(groupId)

        // Then
        assertTrue(result is LeaveGroupUseCase.Result.Success)
        coVerify { mockGroupRepository.leaveGroup(groupId) }
        coVerify { mockUserStateHolder.deleteGroup(groupId) }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val expectedException = Exception("Database error")

        coEvery { mockGroupRepository.leaveGroup(groupId) } throws expectedException

        // When
        val result = useCase(groupId)

        // Then
        assertTrue(result is LeaveGroupUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        coVerify { mockUserStateHolder.deleteGroup(any()) }.wasNotInvoked()
    }
}
