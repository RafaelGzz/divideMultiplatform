package com.ragl.divide.domain.usecases.user

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

class UpdateUserNameUseCaseTest {

    private val mockUserRepository = MockUserRepository()
    private val useCase = UpdateUserNameUseCase(mockUserRepository)

    @Test
    fun `should return success when user name is updated successfully`() = runTest {
        // Given
        val newName = "New User Name"
        mockUserRepository.shouldReturnUpdated = true

        // When
        val result = useCase(newName)

        // Then
        assertTrue(result is UpdateUserNameUseCase.Result.Success)
        assertEquals(true, result.updated)
        assertEquals(newName, mockUserRepository.receivedNewName)
    }

    @Test
    fun `should return success with false when user name is not updated`() = runTest {
        // Given
        val newName = "New User Name"
        mockUserRepository.shouldReturnUpdated = false

        // When
        val result = useCase(newName)

        // Then
        assertTrue(result is UpdateUserNameUseCase.Result.Success)
        assertEquals(false, result.updated)
        assertEquals(newName, mockUserRepository.receivedNewName)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val newName = "New User Name"
        val expectedException = Exception("Database error")
        mockUserRepository.shouldThrowException = expectedException

        // When
        val result = useCase(newName)

        // Then
        assertTrue(result is UpdateUserNameUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
    }

    private class MockUserRepository : UserRepository {
        var shouldReturnUpdated: Boolean = false
        var shouldThrowException: Exception? = null
        var receivedNewName: String? = null

        override suspend fun updateUserName(newName: String): Boolean {
            shouldThrowException?.let { throw it }
            receivedNewName = newName
            return shouldReturnUpdated
        }

        // Implementaciones vacías para otros métodos no utilizados en el test
        override fun getFirebaseUser(): FirebaseUser? = null
        override suspend fun createUserInDatabase(): User = User()
        override suspend fun getUser(id: String): User = User()
        override suspend fun signInWithEmailAndPassword(email: String, password: String): User? = null
        override suspend fun signUpWithEmailAndPassword(email: String, password: String, name: String): User? = null
        override suspend fun signOut() {}
        override suspend fun getExpense(id: String): Expense = Expense()
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
    }
} 