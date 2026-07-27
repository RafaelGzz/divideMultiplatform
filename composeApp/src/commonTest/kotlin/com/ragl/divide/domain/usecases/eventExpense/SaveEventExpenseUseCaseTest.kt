package com.ragl.divide.domain.usecases.eventExpense

import com.ragl.divide.data.models.EventExpense
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

class SaveEventExpenseUseCaseTest {

    private val mockGroupRepository = FakeGroupRepository()
    private val mockUserStateHolder = FakeUserStateHolder()
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

        mockGroupRepository.onSaveEventExpense = { _, _ -> savedEventExpense }
        mockUserStateHolder.onSaveEventExpense = { _, _ -> }

        // When
        val result = useCase(groupId, eventExpense)

        // Then
        assertTrue(result is SaveEventExpenseUseCase.Result.Success)
        assertEquals(savedEventExpense, (result as SaveEventExpenseUseCase.Result.Success).expense)
        assertCalled(mockGroupRepository, "saveEventExpense", groupId, eventExpense)
        assertCalled(mockUserStateHolder, "saveEventExpense", groupId, savedEventExpense)
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

        mockGroupRepository.onSaveEventExpense = { _, _ -> throw expectedException }

        // When
        val result = useCase(groupId, eventExpense)

        // Then
        assertTrue(result is SaveEventExpenseUseCase.Result.Error)
        assertEquals(expectedException, (result as SaveEventExpenseUseCase.Result.Error).exception)
        assertNotCalled(mockUserStateHolder, "saveEventExpense")
    }
}
