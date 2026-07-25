package com.ragl.divide.domain.usecases.group

import com.ragl.divide.data.models.Group
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import io.mockative.any
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RefreshGroupUseCaseTest {

    private val groupRepository = mock(of<GroupRepository>())
    private val userStateHolder = mock(of<UserStateHolder>())
    private val useCase = RefreshGroupUseCase(groupRepository, userStateHolder)

    @Test
    fun `should return success with group when refresh is successful`() = runTest {
        // Given
        val groupId = "group123"
        val expectedGroup = Group(id = groupId, name = "Test Group")
        coEvery { groupRepository.getGroup(groupId) } returns expectedGroup
        coEvery { userStateHolder.updateGroupInState(groupId, expectedGroup) } returns Unit

        // When
        val result = useCase(groupId)

        // Then
        assertTrue(result is RefreshGroupUseCase.Result.Success)
        assertEquals(expectedGroup, result.group)
        coVerify { groupRepository.getGroup(groupId) }
        coVerify { userStateHolder.updateGroupInState(groupId, expectedGroup) }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val expectedException = Exception("Database error")
        coEvery { groupRepository.getGroup(groupId) } throws expectedException

        // When
        val result = useCase(groupId)

        // Then
        assertTrue(result is RefreshGroupUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        coVerify { userStateHolder.updateGroupInState(any(), any()) }.wasNotInvoked()
    }
}
