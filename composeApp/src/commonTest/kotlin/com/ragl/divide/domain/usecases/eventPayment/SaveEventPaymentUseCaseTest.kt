package com.ragl.divide.domain.usecases.eventPayment

import com.ragl.divide.data.models.EventPayment
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

class SaveEventPaymentUseCaseTest {

    private val mockGroupRepository = FakeGroupRepository()
    private val mockUserStateHolder = FakeUserStateHolder()
    private lateinit var useCase: SaveEventPaymentUseCase

    @BeforeTest
    fun setUp() {
        useCase = SaveEventPaymentUseCase(mockGroupRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when event payment is saved successfully`() = runTest {
        // Given
        val groupId = "group123"
        val eventPayment = EventPayment(
            id = "payment123",
            amount = 50.0,
            eventId = "event123",
        )
        val savedEventPayment = eventPayment.copy(id = "saved_payment_id")

        mockGroupRepository.onSaveEventPayment = { _, _ -> savedEventPayment }
        mockUserStateHolder.onSaveEventPayment = { _, _ -> }

        // When
        val result = useCase(groupId, eventPayment)

        // Then
        assertTrue(result is SaveEventPaymentUseCase.Result.Success)
        assertEquals(savedEventPayment, (result as SaveEventPaymentUseCase.Result.Success).payment)
        assertCalled(mockGroupRepository, "saveEventPayment", groupId, eventPayment)
        assertCalled(mockUserStateHolder, "saveEventPayment", groupId, savedEventPayment)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val eventPayment = EventPayment(
            id = "payment123",
            amount = 50.0,
            eventId = "event123",
        )
        val expectedException = Exception("Database error")

        mockGroupRepository.onSaveEventPayment = { _, _ -> throw expectedException }

        // When
        val result = useCase(groupId, eventPayment)

        // Then
        assertTrue(result is SaveEventPaymentUseCase.Result.Error)
        assertEquals(expectedException, (result as SaveEventPaymentUseCase.Result.Error).exception)
        assertNotCalled(mockUserStateHolder, "saveEventPayment")
    }
}
