package com.ragl.divide.domain.usecases.eventExpense

import com.ragl.divide.data.models.EventExpense
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

class DeleteEventExpenseUseCaseTest {

    private val mockGroupRepository = mock(of<GroupRepository>())
    private val mockUserStateHolder = mock(of<UserStateHolder>())
    private lateinit var useCase: DeleteEventExpenseUseCase

    @BeforeTest
    fun setUp() {
        useCase = DeleteEventExpenseUseCase(mockGroupRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when event expense is deleted successfully`() = runTest {
        // Given
        val groupId = "group123"
        val eventExpense = EventExpense(
            id = "expense123",
            title = "Test Expense",
            amount = 100.0,
            eventId = "event123"
        )

        coEvery { mockGroupRepository.deleteEventExpense(groupId, eventExpense) } returns Unit
        coEvery { mockUserStateHolder.deleteEventExpense(groupId, eventExpense) } returns Unit

        // When
        val result = useCase(groupId, eventExpense)

        // Then
        assertTrue(result is DeleteEventExpenseUseCase.Result.Success)
        coVerify { mockGroupRepository.deleteEventExpense(groupId, eventExpense) }
        coVerify { mockUserStateHolder.deleteEventExpense(groupId, eventExpense) }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val eventExpense = EventExpense(
            id = "expense123",
            title = "Test Expense",
            amount = 100.0,
            eventId = "event123"
        )
        val expectedException = Exception("Database error")

        coEvery { mockGroupRepository.deleteEventExpense(groupId, eventExpense) } throws expectedException

        // When
        val result = useCase(groupId, eventExpense)

        // Then
        assertTrue(result is DeleteEventExpenseUseCase.Result.Error)
        assertEquals(expectedException, (result as DeleteEventExpenseUseCase.Result.Error).exception)
        coVerify { mockUserStateHolder.deleteEventExpense(any(), any()) }.wasNotInvoked()
    }
}
