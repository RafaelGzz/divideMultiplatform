package com.ragl.divide.domain.usecases.payment

import com.ragl.divide.data.models.Expense
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.testing.FakeUserRepository
import com.ragl.divide.testing.FakeUserStateHolder
import com.ragl.divide.testing.assertCalled
import com.ragl.divide.testing.assertNotCalled
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SaveExpensePaymentUseCaseTest {

    private val mockUserRepository = FakeUserRepository()
    private val mockUserStateHolder = FakeUserStateHolder()
    private lateinit var useCase: SaveExpensePaymentUseCase

    @BeforeTest
    fun setUp() {
        useCase = SaveExpensePaymentUseCase(mockUserRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success with saved payment when payment is saved successfully`() = runTest {
        // Given
        val expense = Expense(
            id = "expense123",
            title = "Test Expense",
            amount = 100.0
        )
        val savedExpense = expense.copy(id = "saved_expense_id")

        mockUserRepository.onSaveExpense = { savedExpense }
        mockUserStateHolder.onSaveExpense = { }

        // When
        val result = useCase(expense)

        // Then
        assertTrue(result is SaveExpensePaymentUseCase.Result.Success)
        assertCalled(mockUserRepository, "saveExpense", expense)
        assertCalled(mockUserStateHolder, "saveExpense", expense)
    }

    @Test
    fun `should return success with paid payment when payment is saved successfully`() = runTest {
        // Given
        val expense = Expense(
            id = "expense123",
            title = "Test Expense",
            amount = 100.0,
            paid = true
        )
        val savedExpense = expense.copy(id = "saved_expense_id")

        mockUserRepository.onSaveExpense = { savedExpense }
        mockUserStateHolder.onSaveExpense = { }

        // When
        val result = useCase(expense)

        // Then
        assertTrue(result is SaveExpensePaymentUseCase.Result.Paid)
        assertCalled(mockUserRepository, "saveExpense", expense)
        assertCalled(mockUserStateHolder, "saveExpense", expense)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val expense = Expense(
            id = "expense123",
            title = "Test Expense",
            amount = 100.0
        )
        val expectedException = Exception("Database error")

        mockUserRepository.onSaveExpense = { throw expectedException }

        // When
        val result = useCase(expense)

        // Then
        assertTrue(result is SaveExpensePaymentUseCase.Result.Error)
        assertEquals(expectedException, result.exception)

        assertNotCalled(mockUserStateHolder, "saveExpense")
    }
}
