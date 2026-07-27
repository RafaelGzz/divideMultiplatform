package com.ragl.divide.domain.usecases.event

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

class DeleteEventUseCaseTest {

    private val mockGroupRepository = FakeGroupRepository()
    private val mockUserStateHolder = FakeUserStateHolder()
    private lateinit var useCase: DeleteEventUseCase

    @BeforeTest
    fun setUp() {
        useCase = DeleteEventUseCase(mockGroupRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when event is deleted successfully`() = runTest {
        // Given
        val groupId = "group123"
        val eventId = "event123"

        mockGroupRepository.onDeleteEvent = { _, _ -> }
        mockUserStateHolder.onDeleteEvent = { _, _ -> }

        // When
        val result = useCase(groupId, eventId)

        // Then
        assertTrue(result is DeleteEventUseCase.Result.Success)
        assertCalled(mockGroupRepository, "deleteEvent", groupId, eventId)
        assertCalled(mockUserStateHolder, "deleteEvent", groupId, eventId)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val eventId = "event123"
        val expectedException = Exception("Database error")

        mockGroupRepository.onDeleteEvent = { _, _ -> throw expectedException }

        // When
        val result = useCase(groupId, eventId)

        // Then
        assertTrue(result is DeleteEventUseCase.Result.Error)
        assertEquals(expectedException, (result as DeleteEventUseCase.Result.Error).exception)
        assertNotCalled(mockUserStateHolder, "deleteEvent")
    }
}
