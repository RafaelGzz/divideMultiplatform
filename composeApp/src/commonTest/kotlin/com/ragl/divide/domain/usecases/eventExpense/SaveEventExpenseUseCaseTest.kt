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

class SaveEventExpenseUseCaseTest {

    private val mockGroupRepository = mock(of<GroupRepository>())
    private val mockUserStateHolder = mock(of<UserStateHolder>())
    private lateinit var useCase: SaveEventExpenseUseCase

    @BeforeTest
    fun setUp() {
        useCase = SaveEventExpenseUseCase(mockGroupRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when event expense is saved successfully`() = runTest {
        // Given
        val groupId = "group123"
        val eventExpense = EventExpense(
            id = "expense123",
            title = "Test Expense",
            amount = 100.0,
            eventId = "event123"
        )
        val savedEventExpense = eventExpense.copy(id = "saved_expense_id")

        coEvery { mockGroupRepository.saveEventExpense(groupId, eventExpense) } returns savedEventExpense
        coEvery { mockUserStateHolder.saveEventExpense(groupId, savedEventExpense) } returns Unit

        // When
        val result = useCase(groupId, eventExpense)

        // Then
        assertTrue(result is SaveEventExpenseUseCase.Result.Success)
        assertEquals(savedEventExpense, (result as SaveEventExpenseUseCase.Result.Success).expense)
        coVerify { mockGroupRepository.saveEventExpense(groupId, eventExpense) }
        coVerify { mockUserStateHolder.saveEventExpense(groupId, savedEventExpense) }
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

        coEvery { mockGroupRepository.saveEventExpense(groupId, eventExpense) } throws expectedException

        // When
        val result = useCase(groupId, eventExpense)

        // Then
        assertTrue(result is SaveEventExpenseUseCase.Result.Error)
        assertEquals(expectedException, (result as SaveEventExpenseUseCase.Result.Error).exception)
        coVerify { mockUserStateHolder.saveEventExpense(any(), any()) }.wasNotInvoked()
    }
}
