package com.ragl.divide.domain.usecases.payment

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

class SaveExpensePaymentUseCaseTest {

    private val mockUserRepository = mock(of<UserRepository>())
    private val mockUserStateHolder = mock(of<UserStateHolder>())
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

        coEvery { mockUserRepository.saveExpense(expense) } returns savedExpense
        coEvery { mockUserStateHolder.saveExpense(expense) } returns Unit

        // When
        val result = useCase(expense)

        // Then
        assertTrue(result is SaveExpensePaymentUseCase.Result.Success)
        coVerify { mockUserRepository.saveExpense(any()) }
        coVerify { mockUserStateHolder.saveExpense(any()) }
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

        coEvery { mockUserRepository.saveExpense(expense) } returns savedExpense
        coEvery { mockUserStateHolder.saveExpense(expense) } returns Unit

        // When
        val result = useCase(expense)

        // Then
        assertTrue(result is SaveExpensePaymentUseCase.Result.Paid)
        coVerify { mockUserRepository.saveExpense(any()) }
        coVerify { mockUserStateHolder.saveExpense(any()) }
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
        assertTrue(result is SaveExpensePaymentUseCase.Result.Error)
        assertEquals(expectedException, result.exception)

        coVerify { mockUserStateHolder.saveExpense(any()) }.wasNotInvoked()
    }
}
