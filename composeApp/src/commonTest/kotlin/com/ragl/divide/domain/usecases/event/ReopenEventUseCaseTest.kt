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

class ReopenEventUseCaseTest {

    private val mockGroupRepository = FakeGroupRepository()
    private val mockUserStateHolder = FakeUserStateHolder()
    private lateinit var useCase: ReopenEventUseCase

    @BeforeTest
    fun setUp() {
        useCase = ReopenEventUseCase(mockGroupRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when event is reopened successfully`() = runTest {
        // Given
        val groupId = "group123"
        val eventId = "event123"

        mockGroupRepository.onReopenEvent = { _, _ -> }
        mockUserStateHolder.onReopenEvent = { _, _ -> }

        // When
        val result = useCase(groupId, eventId)

        // Then
        assertTrue(result is ReopenEventUseCase.Result.Success)
        assertCalled(mockGroupRepository, "reopenEvent", groupId, eventId)
        assertCalled(mockUserStateHolder, "reopenEvent", groupId, eventId)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val eventId = "event123"
        val expectedException = Exception("Database error")

        mockGroupRepository.onReopenEvent = { _, _ -> throw expectedException }

        // When
        val result = useCase(groupId, eventId)

        // Then
        assertTrue(result is ReopenEventUseCase.Result.Error)
        assertEquals(expectedException, (result as ReopenEventUseCase.Result.Error).exception)
        assertNotCalled(mockUserStateHolder, "reopenEvent")
    }
}
