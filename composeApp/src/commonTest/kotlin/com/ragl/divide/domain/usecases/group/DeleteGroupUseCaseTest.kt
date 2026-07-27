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

class DeleteGroupUseCaseTest {

    private val mockGroupRepository = FakeGroupRepository()
    private val mockUserStateHolder = FakeUserStateHolder()
    private lateinit var useCase: DeleteGroupUseCase

    @BeforeTest
    fun setUp() {
        useCase = DeleteGroupUseCase(mockGroupRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when group is deleted successfully`() = runTest {
        // Given
        val groupId = "group123"
        val groupImage = "image.jpg"

        mockGroupRepository.onDeleteGroup = { _, _ -> }
        mockUserStateHolder.onDeleteGroup = { }

        // When
        val result = useCase(groupId, groupImage)

        // Then
        assertTrue(result is DeleteGroupUseCase.Result.Success)
        assertCalled(mockGroupRepository, "deleteGroup", groupId, groupImage)
        assertCalled(mockUserStateHolder, "deleteGroup", groupId)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val groupImage = "image.jpg"
        val expectedException = Exception("Database error")

        mockGroupRepository.onDeleteGroup = { _, _ -> throw expectedException }

        // When
        val result = useCase(groupId, groupImage)

        // Then
        assertTrue(result is DeleteGroupUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        assertNotCalled(mockUserStateHolder, "deleteGroup")

    }
}
