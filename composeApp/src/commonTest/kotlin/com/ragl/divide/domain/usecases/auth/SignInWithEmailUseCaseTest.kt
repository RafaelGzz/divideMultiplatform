package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.AnalyticsService
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignInWithEmailUseCaseTest {

    private val mockUserRepository = MockUserRepository()
    private val mockAnalyticsService = MockAnalyticsService()
    private val useCase = SignInWithEmailUseCase(mockUserRepository, mockAnalyticsService)

    @Test
    fun `should return success when login is successful and email is verified`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val expectedUser = User(
            uuid = "user123",
            email = email,
            name = "Test User"
        )
        
        mockUserRepository.shouldReturnUser = expectedUser
        mockUserRepository.shouldReturnEmailVerified = true

        // When
        val result = useCase(email, password)

        // Then
        assertTrue(result is SignInWithEmailUseCase.Result.Success)
        assertEquals(expectedUser, result.user)
        assertEquals(1, mockAnalyticsService.loggedEvents.size)
        assertEquals("login", mockAnalyticsService.loggedEvents[0].first)
    }

    @Test
    fun `should return EmailNotVerified when email is not verified`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val user = User(uuid = "user123", email = email, name = "Test User")
        
        mockUserRepository.shouldReturnUser = user
        mockUserRepository.shouldReturnEmailVerified = false

        // When
        val result = useCase(email, password)

        // Then
        assertTrue(result is SignInWithEmailUseCase.Result.EmailNotVerified)
    }

    @Test
    fun `should return error when user repository returns null`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        
        mockUserRepository.shouldReturnUser = null

        // When
        val result = useCase(email, password)

        // Then
        assertTrue(result is SignInWithEmailUseCase.Result.Error)
        assertEquals("Failed to login", result.exception.message)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val expectedException = Exception("Network error")
        
        mockUserRepository.shouldThrowException = expectedException

        // When
        val result = useCase(email, password)

        // Then
        assertTrue(result is SignInWithEmailUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        assertEquals(1, mockAnalyticsService.loggedErrors.size)
    }

    @Test
    fun `should log analytics event on successful login`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val user = User(uuid = "user123", email = email, name = "Test User")
        
        mockUserRepository.shouldReturnUser = user
        mockUserRepository.shouldReturnEmailVerified = true

        // When
        useCase(email, password)

        // Then
        assertEquals(1, mockAnalyticsService.loggedEvents.size)
        val (eventName, parameters) = mockAnalyticsService.loggedEvents[0]
        assertEquals("login", eventName)
        assertEquals("email", parameters["method"])
    }

    private class MockUserRepository : UserRepository {
        var shouldReturnUser: User? = null
        var shouldReturnEmailVerified: Boolean = true
        var shouldThrowException: Exception? = null

        override suspend fun signInWithEmailAndPassword(email: String, password: String): User? {
            shouldThrowException?.let { throw it }
            return shouldReturnUser
        }

        override suspend fun isEmailVerified(): Boolean {
            return shouldReturnEmailVerified
        }

        // Implementaciones vacías para otros métodos no utilizados en el test
        override fun getFirebaseUser(): FirebaseUser? = null
        override suspend fun createUserInDatabase(): User = User()
        override suspend fun getUser(id: String): User = User()
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
        override suspend fun saveProfilePhoto(photo: File): String = ""
        override suspend fun getProfilePhoto(userId: String): String = ""
        override suspend fun updateUserName(newName: String): Boolean = false
    }

    private class MockAnalyticsService : AnalyticsService {
        val loggedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        val loggedErrors = mutableListOf<Pair<Throwable, String?>>()
        
        override fun setUserProperties(userId: String, userName: String) {}
        override fun logEvent(eventName: String, params: Map<String, Any>) {
            loggedEvents.add(eventName to params)
        }
        override fun logError(throwable: Throwable, message: String?) {
            loggedErrors.add(throwable to message)
        }
    }
} 