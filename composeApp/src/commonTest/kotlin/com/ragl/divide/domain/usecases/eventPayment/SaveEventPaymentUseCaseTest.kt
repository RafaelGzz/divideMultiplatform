package com.ragl.divide.domain.usecases.eventPayment

import com.ragl.divide.data.models.EventPayment
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

class SaveEventPaymentUseCaseTest {

    private val mockGroupRepository = mock(of<GroupRepository>())
    private val mockUserStateHolder = mock(of<UserStateHolder>())
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

        coEvery { mockGroupRepository.saveEventPayment(groupId, eventPayment) } returns savedEventPayment
        coEvery { mockUserStateHolder.saveEventPayment(groupId, savedEventPayment) } returns Unit

        // When
        val result = useCase(groupId, eventPayment)

        // Then
        assertTrue(result is SaveEventPaymentUseCase.Result.Success)
        assertEquals(savedEventPayment, (result as SaveEventPaymentUseCase.Result.Success).payment)
        coVerify { mockGroupRepository.saveEventPayment(groupId, eventPayment) }
        coVerify { mockUserStateHolder.saveEventPayment(groupId, savedEventPayment) }
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

        coEvery { mockGroupRepository.saveEventPayment(groupId, eventPayment) } throws expectedException

        // When
        val result = useCase(groupId, eventPayment)

        // Then
        assertTrue(result is SaveEventPaymentUseCase.Result.Error)
        assertEquals(expectedException, (result as SaveEventPaymentUseCase.Result.Error).exception)
        coVerify { mockUserStateHolder.saveEventPayment(any(), any()) }.wasNotInvoked()
    }
}
