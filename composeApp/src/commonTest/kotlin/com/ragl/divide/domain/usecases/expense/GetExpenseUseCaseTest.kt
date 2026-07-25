package com.ragl.divide.domain.usecases.expense

import com.ragl.divide.data.models.Expense
import com.ragl.divide.domain.repositories.UserRepository
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetExpenseUseCaseTest {

    private val mockUserRepository = mock(of<UserRepository>())
    private lateinit var useCase: GetExpenseUseCase

    @BeforeTest
    fun setUp() {
        useCase = GetExpenseUseCase(mockUserRepository)
    }

    @Test
    fun `should return success with expense when expense is found`() = runTest {
        // Given
        val expenseId = "expense123"
        val expectedExpense = Expense(
            id = expenseId,
            title = "Test Expense",
            amount = 100.0
        )

        coEvery { mockUserRepository.getExpense(expenseId) } returns expectedExpense

        // When
        val result = useCase(expenseId)

        // Then
        assertTrue(result is GetExpenseUseCase.Result.Success)
        assertEquals(expectedExpense, result.expense)
        coVerify { mockUserRepository.getExpense(expenseId) }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val expenseId = "expense123"
        val expectedException = Exception("Database error")

        coEvery { mockUserRepository.getExpense(expenseId) } throws expectedException

        // When
        val result = useCase(expenseId)

        // Then
        assertTrue(result is GetExpenseUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
    }
}
