package com.ragl.divide.domain.usecases.group

import com.ragl.divide.data.models.Group
import com.ragl.divide.domain.repositories.FriendsRepository
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import dev.gitlive.firebase.storage.File
import io.mockative.any
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.every
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SaveGroupUseCaseTest {

    private val mockGroupRepository = mock(of<GroupRepository>())
    private val mockFriendsRepository = mock(of<FriendsRepository>())
    private val mockUserStateHolder = mock(of<UserStateHolder>())
    private lateinit var useCase: SaveGroupUseCase

    @BeforeTest
    fun setUp() {
        useCase = SaveGroupUseCase(mockGroupRepository, mockFriendsRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when group is saved successfully`() = runTest {
        // Given
        val group = Group(id = "group123", name = "Test Group")
        val photo: File? = null
        val savedGroup = group.copy()

        coEvery { mockGroupRepository.saveGroup(group, photo) } returns savedGroup
        every { mockUserStateHolder.saveGroup(savedGroup) } returns Unit
        every { mockUserStateHolder.getGroupMembers("group123") } returns listOf()

        // When
        val result = useCase(group, photo)

        print(result)

        // Then
        assertTrue(result is SaveGroupUseCase.Result.Success)
        coVerify { mockGroupRepository.saveGroup(group, photo) }
        coVerify { mockUserStateHolder.saveGroup(savedGroup) }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val group = Group(id = "group123", name = "Test Group")
        val photo: File? = null
        val expectedException = Exception("Database error")

        coEvery { mockGroupRepository.saveGroup(group, photo) } throws expectedException

        // When
        val result = useCase(group, photo)

        // Then
        assertTrue(result is SaveGroupUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        coVerify { mockUserStateHolder.saveGroup(any()) }.wasNotInvoked()
    }
}
