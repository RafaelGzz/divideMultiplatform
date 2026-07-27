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

class SettleEventUseCaseTest {

    private val mockGroupRepository = FakeGroupRepository()
    private val mockUserStateHolder = FakeUserStateHolder()
    private lateinit var useCase: SettleEventUseCase

    @BeforeTest
    fun setUp() {
        useCase = SettleEventUseCase(mockGroupRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when event is settled successfully`() = runTest {
        // Given
        val groupId = "group123"
        val eventId = "event123"

        mockGroupRepository.onSettleEvent = { _, _ -> }
        mockUserStateHolder.onSettleEvent = { _, _ -> }

        // When
        val result = useCase(groupId, eventId)

        // Then
        assertTrue(result is SettleEventUseCase.Result.Success)
        assertCalled(mockGroupRepository, "settleEvent", groupId, eventId)
        assertCalled(mockUserStateHolder, "settleEvent", groupId, eventId)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val eventId = "event123"
        val expectedException = Exception("Database error")

        mockGroupRepository.onSettleEvent = { _, _ -> throw expectedException }

        // When
        val result = useCase(groupId, eventId)

        // Then
        assertTrue(result is SettleEventUseCase.Result.Error)
        assertEquals(expectedException, (result as SettleEventUseCase.Result.Error).exception)
        assertNotCalled(mockUserStateHolder, "settleEvent")
    }
}
