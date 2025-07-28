package com.ragl.divide.domain.usecases.expense

import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import com.ragl.divide.domain.repositories.UserRepository
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetExpenseUseCaseTest {

    private val mockUserRepository = MockUserRepository()
    private val useCase = GetExpenseUseCase(mockUserRepository)

    @Test
    fun `should return success with expense when fetch is successful`() = runTest {
        // Given
        val expenseId = "expense123"
        val expectedExpense = Expense(
            id = expenseId,
            title = "Test Expense",
            amount = 100.0
        )
        mockUserRepository.shouldReturnExpense = expectedExpense

        // When
        val result = useCase(expenseId)

        // Then
        assertTrue(result is GetExpenseUseCase.Result.Success)
        assertEquals(expectedExpense, result.expense)
        assertEquals(expenseId, mockUserRepository.receivedExpenseId)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val expenseId = "expense123"
        val expectedException = Exception("Database error")
        mockUserRepository.shouldThrowException = expectedException

        // When
        val result = useCase(expenseId)

        // Then
        assertTrue(result is GetExpenseUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
    }

    private class MockUserRepository : UserRepository {
        var shouldReturnExpense: Expense? = null
        var shouldThrowException: Exception? = null
        var receivedExpenseId: String? = null

        override suspend fun getExpense(id: String): Expense {
            shouldThrowException?.let { throw it }
            receivedExpenseId = id
            return shouldReturnExpense ?: Expense()
        }

        // Implementaciones vacías para otros métodos no utilizados en el test
        override fun getFirebaseUser(): FirebaseUser? = null
        override suspend fun createUserInDatabase(): User = User()
        override suspend fun getUser(id: String): User = User()
        override suspend fun signInWithEmailAndPassword(email: String, password: String): User? = null
        override suspend fun signUpWithEmailAndPassword(email: String, password: String, name: String): User? = null
        override suspend fun signOut() {}
        override suspend fun getExpenses(): Map<String, Expense> = emptyMap()
        override suspend fun saveExpense(expense: Expense): Expense = expense
        override suspend fun deleteExpense(id: String) {}
        override suspend fun getExpensePayments(expenseId: String): Map<String, Payment> = emptyMap()
        override suspend fun saveExpensePayment(payment: Payment, expenseId: String, expensePaid: Boolean): Payment = payment
        override suspend fun deleteExpensePayment(paymentId: String, amount: Double, expenseId: String) {}
        override suspend fun addGroupToUser(id: String, userId: String) {}
        override suspend fun removeGroupFromUser(groupId: String, userId: String) {}
        override suspend fun sendEmailVerification() {}
        override suspend fun isEmailVerified(): Boolean = true
        override suspend fun saveProfilePhoto(photo: File): String = ""
        override suspend fun getProfilePhoto(userId: String): String = ""
        override suspend fun updateUserName(newName: String): Boolean = true
    }
} 