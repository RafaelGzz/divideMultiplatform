package com.ragl.divide.domain.usecases.event

import com.ragl.divide.data.models.Event
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

class SaveEventUseCaseTest {

    private val mockGroupRepository = FakeGroupRepository()
    private val mockUserStateHolder = FakeUserStateHolder()
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

        mockGroupRepository.onSaveEvent = { _, _ -> savedEvent }
        mockUserStateHolder.onSaveEvent = { _, _ -> }

        // When
        val result = useCase(groupId, event)

        // Then
        assertTrue(result is SaveEventUseCase.Result.Success)
        assertEquals(savedEvent, (result as SaveEventUseCase.Result.Success).event)
        assertCalled(mockGroupRepository, "saveEvent", groupId, event)
        assertCalled(mockUserStateHolder, "saveEvent", groupId, savedEvent)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "test_group_id"
        val event = Event(title = "Test Event")
        val expectedException = Exception("Database error")

        mockGroupRepository.onSaveEvent = { _, _ -> throw expectedException }

        // When
        val result = useCase(groupId, event)

        // Then
        assertTrue(result is SaveEventUseCase.Result.Error)
        assertEquals(expectedException, (result as SaveEventUseCase.Result.Error).exception)
        assertNotCalled(mockUserStateHolder, "saveEvent")
    }
}
