package com.ragl.divide.domain.usecases.expense

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

class DeleteExpenseUseCaseTest {

    private val mockUserRepository = FakeUserRepository()
    private val mockUserStateHolder = FakeUserStateHolder()
    private lateinit var useCase: DeleteExpenseUseCase

    @BeforeTest
    fun setUp() {
        useCase = DeleteExpenseUseCase(mockUserRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when expense is deleted successfully`() = runTest {
        // Given
        val expenseId = "expense123"

        mockUserRepository.onDeleteExpense = { }
        mockUserStateHolder.onRemoveExpense = { }

        // When
        val result = useCase(expenseId)

        // Then
        assertTrue(result is DeleteExpenseUseCase.Result.Success)
        assertCalled(mockUserRepository, "deleteExpense", expenseId)
        assertCalled(mockUserStateHolder, "removeExpense", expenseId)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val expenseId = "expense123"
        val expectedException = Exception("Database error")

        mockUserRepository.onDeleteExpense = { throw expectedException }

        // When
        val result = useCase(expenseId)

        // Then
        assertTrue(result is DeleteExpenseUseCase.Result.Error)
        assertEquals(expectedException, (result as DeleteExpenseUseCase.Result.Error).exception)
        assertNotCalled(mockUserStateHolder, "removeExpense")
    }
}
