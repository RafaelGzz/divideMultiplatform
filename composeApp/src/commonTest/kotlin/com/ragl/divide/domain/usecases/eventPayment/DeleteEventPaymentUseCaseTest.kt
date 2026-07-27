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

class DeleteEventPaymentUseCaseTest {

    private val mockGroupRepository = FakeGroupRepository()
    private val mockUserStateHolder = FakeUserStateHolder()
    private lateinit var useCase: DeleteEventPaymentUseCase

    @BeforeTest
    fun setUp() {
        useCase = DeleteEventPaymentUseCase(mockGroupRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when event payment is deleted successfully`() = runTest {
        // Given
        val groupId = "group123"
        val eventPayment = EventPayment(
            id = "payment123",
            amount = 50.0,
            eventId = "event123"
        )

        mockGroupRepository.onDeleteEventPayment = { _, _ -> }
        mockUserStateHolder.onDeleteEventPayment = { _, _ -> }

        // When
        val result = useCase(groupId, eventPayment)

        // Then
        assertTrue(result is DeleteEventPaymentUseCase.Result.Success)
        assertCalled(mockGroupRepository, "deleteEventPayment", groupId, eventPayment)
        assertCalled(mockUserStateHolder, "deleteEventPayment", groupId, eventPayment)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val eventPayment = EventPayment(
            id = "payment123",
            amount = 50.0,
            eventId = "event123"
        )
        val expectedException = Exception("Database error")

        mockGroupRepository.onDeleteEventPayment = { _, _ -> throw expectedException }

        // When
        val result = useCase(groupId, eventPayment)

        // Then
        assertTrue(result is DeleteEventPaymentUseCase.Result.Error)
        assertEquals(expectedException, (result as DeleteEventPaymentUseCase.Result.Error).exception)
        assertNotCalled(mockUserStateHolder, "deleteEventPayment")
    }
}
