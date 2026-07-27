package com.ragl.divide.domain.usecases.group

import com.ragl.divide.data.models.Group
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.testing.FakeGroupRepository
import com.ragl.divide.testing.FakeUserStateHolder
import com.ragl.divide.testing.assertCalled
import com.ragl.divide.testing.assertNotCalled
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RefreshGroupUseCaseTest {

    private val groupRepository = FakeGroupRepository()
    private val userStateHolder = FakeUserStateHolder()
    private val useCase = RefreshGroupUseCase(groupRepository, userStateHolder)

    @Test
    fun `should return success with group when refresh is successful`() = runTest {
        // Given
        val groupId = "group123"
        val expectedGroup = Group(id = groupId, name = "Test Group")
        groupRepository.onGetGroup = { expectedGroup }
        userStateHolder.onUpdateGroupInState = { _, _ -> }

        // When
        val result = useCase(groupId)

        // Then
        assertTrue(result is RefreshGroupUseCase.Result.Success)
        assertEquals(expectedGroup, result.group)
        assertCalled(groupRepository, "getGroup", groupId)
        assertCalled(userStateHolder, "updateGroupInState", groupId, expectedGroup)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val expectedException = Exception("Database error")
        groupRepository.onGetGroup = { throw expectedException }

        // When
        val result = useCase(groupId)

        // Then
        assertTrue(result is RefreshGroupUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        assertNotCalled(userStateHolder, "updateGroupInState")
    }
}
