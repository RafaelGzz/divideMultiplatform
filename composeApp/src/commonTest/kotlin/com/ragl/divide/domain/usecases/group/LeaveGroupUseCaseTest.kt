package com.ragl.divide.domain.usecases.group

import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.testing.FakeGroupRepository
import com.ragl.divide.testing.FakeUserStateHolder
import com.ragl.divide.testing.assertCalled
import com.ragl.divide.testing.assertNotCalled
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaveGroupUseCaseTest {

    private val mockGroupRepository = FakeGroupRepository()
    private val mockUserStateHolder = FakeUserStateHolder()
    private lateinit var useCase: LeaveGroupUseCase

    @BeforeTest
    fun setUp() {
        useCase = LeaveGroupUseCase(mockGroupRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when group is left successfully`() = runTest {
        // Given
        val groupId = "group123"

        mockGroupRepository.onLeaveGroup = { }
        mockUserStateHolder.onDeleteGroup = { }

        // When
        val result = useCase(groupId)

        // Then
        assertTrue(result is LeaveGroupUseCase.Result.Success)
        assertCalled(mockGroupRepository, "leaveGroup", groupId)
        assertCalled(mockUserStateHolder, "deleteGroup", groupId)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val expectedException = Exception("Database error")

        mockGroupRepository.onLeaveGroup = { throw expectedException }

        // When
        val result = useCase(groupId)

        // Then
        assertTrue(result is LeaveGroupUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        assertNotCalled(mockUserStateHolder, "deleteGroup")
    }
}
