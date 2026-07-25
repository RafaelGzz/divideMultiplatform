package com.ragl.divide.domain.usecases.expense

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

class DeleteExpenseUseCaseTest {

    private val mockUserRepository = mock(of<UserRepository>())
    private val mockUserStateHolder = mock(of<UserStateHolder>())
    private lateinit var useCase: DeleteExpenseUseCase

    @BeforeTest
    fun setUp() {
        useCase = DeleteExpenseUseCase(mockUserRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when expense is deleted successfully`() = runTest {
        // Given
        val expenseId = "expense123"

        coEvery { mockUserRepository.deleteExpense(expenseId) } returns Unit
        coEvery { mockUserStateHolder.removeExpense(expenseId) } returns Unit

        // When
        val result = useCase(expenseId)

        // Then
        assertTrue(result is DeleteExpenseUseCase.Result.Success)
        coVerify { mockUserRepository.deleteExpense(expenseId) }
        coVerify { mockUserStateHolder.removeExpense(expenseId) }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val expenseId = "expense123"
        val expectedException = Exception("Database error")

        coEvery { mockUserRepository.deleteExpense(expenseId) } throws expectedException

        // When
        val result = useCase(expenseId)

        // Then
        assertTrue(result is DeleteExpenseUseCase.Result.Error)
        assertEquals(expectedException, (result as DeleteExpenseUseCase.Result.Error).exception)
        coVerify { mockUserStateHolder.removeExpense(any()) }.wasNotInvoked()
    }
}
