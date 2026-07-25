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

class DeleteGroupUseCaseTest {

    private val mockGroupRepository = mock(of<GroupRepository>())
    private val mockUserStateHolder = mock(of<UserStateHolder>())
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

        coEvery { mockGroupRepository.deleteGroup(groupId, groupImage) } returns Unit
        coEvery { mockUserStateHolder.deleteGroup(groupId) } returns Unit

        // When
        val result = useCase(groupId, groupImage)

        // Then
        assertTrue(result is DeleteGroupUseCase.Result.Success)
        coVerify { mockGroupRepository.deleteGroup(groupId, groupImage) }
        coVerify { mockUserStateHolder.deleteGroup(groupId) }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val groupImage = "image.jpg"
        val expectedException = Exception("Database error")

        coEvery { mockGroupRepository.deleteGroup(groupId, groupImage) } throws expectedException

        // When
        val result = useCase(groupId, groupImage)

        // Then
        assertTrue(result is DeleteGroupUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        coVerify { mockUserStateHolder.deleteGroup(any()) }.wasNotInvoked()

    }
}
