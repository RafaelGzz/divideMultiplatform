package com.ragl.divide.domain.usecases.expense

import com.ragl.divide.data.models.Expense
import com.ragl.divide.domain.repositories.UserRepository
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

class SaveExpenseUseCaseTest {

    private val mockUserRepository = mock(of<UserRepository>())
    private val mockUserStateHolder = mock(of<UserStateHolder>())
    private lateinit var useCase: SaveExpenseUseCase

    @BeforeTest
    fun setUp() {
        useCase = SaveExpenseUseCase(mockUserRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success with saved expense when expense is saved successfully`() = runTest {
        // Given
        val expense = Expense(
            id = "expense123",
            title = "Test Expense",
            amount = 100.0
        )
        val savedExpense = expense.copy(id = "saved_expense_id")

        coEvery { mockUserRepository.saveExpense(expense) } returns savedExpense
        coEvery { mockUserStateHolder.saveExpense(savedExpense) } returns Unit

        // When
        val result = useCase(expense)

        // Then
        assertTrue(result is SaveExpenseUseCase.Result.Success)
        assertEquals(savedExpense, result.expense)
        coVerify { mockUserRepository.saveExpense(expense) }
        coVerify { mockUserStateHolder.saveExpense(savedExpense) }
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

        coEvery { mockUserRepository.saveExpense(expense) } throws expectedException

        // When
        val result = useCase(expense)

        // Then
        assertTrue(result is SaveExpenseUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        coVerify { mockUserStateHolder.saveExpense(any()) }.wasNotInvoked()
    }
}
