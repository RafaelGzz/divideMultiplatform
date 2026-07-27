package com.ragl.divide.domain.usecases.group

import com.ragl.divide.data.models.Group
import com.ragl.divide.domain.repositories.FriendsRepository
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import dev.gitlive.firebase.storage.File
import com.ragl.divide.testing.FakeFriendsRepository
import com.ragl.divide.testing.FakeGroupRepository
import com.ragl.divide.testing.FakeUserStateHolder
import com.ragl.divide.testing.assertCalled
import com.ragl.divide.testing.assertNotCalled
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SaveGroupUseCaseTest {

    private val mockGroupRepository = FakeGroupRepository()
    private val mockFriendsRepository = FakeFriendsRepository()
    private val mockUserStateHolder = FakeUserStateHolder()
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

        mockGroupRepository.onSaveGroup = { _, _ -> savedGroup }
        mockUserStateHolder.onSaveGroup = { }
        mockUserStateHolder.onGetGroupMembers = { emptyList() }

        // When
        val result = useCase(group, photo)

        print(result)

        // Then
        assertTrue(result is SaveGroupUseCase.Result.Success)
        assertCalled(mockGroupRepository, "saveGroup", group, photo)
        assertCalled(mockUserStateHolder, "saveGroup", savedGroup)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val group = Group(id = "group123", name = "Test Group")
        val photo: File? = null
        val expectedException = Exception("Database error")

        mockGroupRepository.onSaveGroup = { _, _ -> throw expectedException }

        // When
        val result = useCase(group, photo)

        // Then
        assertTrue(result is SaveGroupUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        assertNotCalled(mockUserStateHolder, "saveGroup")
    }
}
