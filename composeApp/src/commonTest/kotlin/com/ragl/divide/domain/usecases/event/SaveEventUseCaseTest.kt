package com.ragl.divide.domain.usecases.event

import com.ragl.divide.data.models.Event
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

class SaveEventUseCaseTest {

    private val mockGroupRepository = mock(of<GroupRepository>())
    private val mockUserStateHolder = mock(of<UserStateHolder>())
    private lateinit var useCase: SaveEventUseCase

    @BeforeTest
    fun setUp() {
        useCase = SaveEventUseCase(mockGroupRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when event is saved successfully`() = runTest {
        // Given
        val groupId = "test_group_id"
        val event = Event(title = "Test Event")
        val savedEvent = event.copy(id = "saved_event_id")

        coEvery { mockGroupRepository.saveEvent(groupId, event) } returns savedEvent
        coEvery { mockUserStateHolder.saveEvent(groupId, savedEvent) } returns Unit

        // When
        val result = useCase(groupId, event)

        // Then
        assertTrue(result is SaveEventUseCase.Result.Success)
        assertEquals(savedEvent, (result as SaveEventUseCase.Result.Success).event)
        coVerify { mockGroupRepository.saveEvent(groupId, event) }
        coVerify { mockUserStateHolder.saveEvent(groupId, savedEvent) }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "test_group_id"
        val event = Event(title = "Test Event")
        val expectedException = Exception("Database error")

        coEvery { mockGroupRepository.saveEvent(groupId, event) } throws expectedException

        // When
        val result = useCase(groupId, event)

        // Then
        assertTrue(result is SaveEventUseCase.Result.Error)
        assertEquals(expectedException, (result as SaveEventUseCase.Result.Error).exception)
        coVerify { mockUserStateHolder.saveEvent(any(), any()) }.wasNotInvoked()
    }
}
