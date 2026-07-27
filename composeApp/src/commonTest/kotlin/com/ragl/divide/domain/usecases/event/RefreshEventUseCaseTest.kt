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

class RefreshEventUseCaseTest {

    private val mockGroupRepository = FakeGroupRepository()
    private val mockUserStateHolder = FakeUserStateHolder()
    private lateinit var useCase: RefreshEventUseCase

    @BeforeTest
    fun setUp() {
        useCase = RefreshEventUseCase(mockGroupRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success with event when refresh is successful`() = runTest {
        // Given
        val groupId = "group123"
        val eventId = "event123"
        val expectedEvent = Event(id = eventId, title = "Test Event")

        mockGroupRepository.onGetEvent = { _, _ -> expectedEvent }
        mockUserStateHolder.onUpdateEventInState = { _, _, _ -> }

        // When
        val result = useCase(groupId, eventId)

        // Then
        assertTrue(result is RefreshEventUseCase.Result.Success)
        assertEquals(expectedEvent, (result as RefreshEventUseCase.Result.Success).event)
        assertCalled(mockGroupRepository, "getEvent", groupId, eventId)
        assertCalled(mockUserStateHolder, "updateEventInState", groupId, eventId, expectedEvent)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val eventId = "event123"
        val expectedException = Exception("Database error")

        mockGroupRepository.onGetEvent = { _, _ -> throw expectedException }

        // When
        val result = useCase(groupId, eventId)

        // Then
        assertTrue(result is RefreshEventUseCase.Result.Error)
        assertEquals(expectedException, (result as RefreshEventUseCase.Result.Error).exception)
        assertNotCalled(mockUserStateHolder, "updateEventInState")
    }
}
