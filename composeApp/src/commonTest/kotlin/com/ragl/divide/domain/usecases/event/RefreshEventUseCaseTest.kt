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

class RefreshEventUseCaseTest {

    private val mockGroupRepository = mock(of<GroupRepository>())
    private val mockUserStateHolder = mock(of<UserStateHolder>())
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

        coEvery { mockGroupRepository.getEvent(groupId, eventId) } returns expectedEvent
        coEvery { mockUserStateHolder.updateEventInState(groupId, eventId, expectedEvent) } returns Unit

        // When
        val result = useCase(groupId, eventId)

        // Then
        assertTrue(result is RefreshEventUseCase.Result.Success)
        assertEquals(expectedEvent, (result as RefreshEventUseCase.Result.Success).event)
        coVerify { mockGroupRepository.getEvent(groupId, eventId) }
        coVerify { mockUserStateHolder.updateEventInState(groupId, eventId, expectedEvent) }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val eventId = "event123"
        val expectedException = Exception("Database error")

        coEvery { mockGroupRepository.getEvent(groupId, eventId) } throws expectedException

        // When
        val result = useCase(groupId, eventId)

        // Then
        assertTrue(result is RefreshEventUseCase.Result.Error)
        assertEquals(expectedException, (result as RefreshEventUseCase.Result.Error).exception)
        coVerify { mockUserStateHolder.updateEventInState(any(), any(), any()) }.wasNotInvoked()
    }
}
