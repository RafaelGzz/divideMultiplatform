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

class SignUpWithEmailUseCaseTest {

    private val mockUserRepository = MockUserRepository()
    private val mockAnalyticsService = MockAnalyticsService()
    private val useCase = SignUpWithEmailUseCase(mockUserRepository, mockAnalyticsService)

    @Test
    fun `should return success when signup is successful`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val name = "Test User"
        val expectedUser = User(
            uuid = "user123",
            email = email,
            name = name
        )
        
        mockUserRepository.shouldReturnUser = expectedUser

        // When
        val result = useCase(email, password, name)

        // Then
        assertTrue(result is SignUpWithEmailUseCase.Result.Success)
        assertEquals(1, mockAnalyticsService.loggedEvents.size)
        assertEquals("sign_up", mockAnalyticsService.loggedEvents[0].first)
        assertEquals("email", mockAnalyticsService.loggedEvents[0].second["method"])
        assertEquals(email, mockAnalyticsService.loggedEvents[0].second["email"])
        assertTrue(mockUserRepository.signOutCalled)
    }

    @Test
    fun `should return error when user repository returns null`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val name = "Test User"
        
        mockUserRepository.shouldReturnUser = null

        // When
        val result = useCase(email, password, name)

        // Then
        assertTrue(result is SignUpWithEmailUseCase.Result.Error)
        assertEquals("Failed to sign up", result.exception.message)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val name = "Test User"
        val expectedException = Exception("Network error")
        
        mockUserRepository.shouldThrowException = expectedException

        // When
        val result = useCase(email, password, name)

        // Then
        assertTrue(result is SignUpWithEmailUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        assertEquals(1, mockAnalyticsService.loggedErrors.size)
        assertEquals("Error en registro con email", mockAnalyticsService.loggedErrors[0].second)
    }

    @Test
    fun `should log analytics event on successful signup`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val name = "Test User"
        val user = User(uuid = "user123", email = email, name = name)
        
        mockUserRepository.shouldReturnUser = user

        // When
        useCase(email, password, name)

        // Then
        assertEquals(1, mockAnalyticsService.loggedEvents.size)
        val (eventName, parameters) = mockAnalyticsService.loggedEvents[0]
        assertEquals("sign_up", eventName)
        assertEquals("email", parameters["method"])
        assertEquals(email, parameters["email"])
    }

    @Test
    fun `should call signOut after successful signup`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val name = "Test User"
        val user = User(uuid = "user123", email = email, name = name)
        
        mockUserRepository.shouldReturnUser = user

        // When
        useCase(email, password, name)

        // Then
        assertTrue(mockUserRepository.signOutCalled)
    }

    private class MockUserRepository : UserRepository {
        var shouldReturnUser: User? = null
        var shouldThrowException: Exception? = null
        var signOutCalled = false

        override suspend fun signUpWithEmailAndPassword(email: String, password: String, name: String): User? {
            shouldThrowException?.let { throw it }
            return shouldReturnUser
        }

        override suspend fun signOut() {
            signOutCalled = true
        }

        // Implementaciones vacías para otros métodos no utilizados en el test
        override fun getCurrentUser(): FirebaseUser? = null
        override suspend fun createUserInDatabase(): User = User()
        override suspend fun getUser(id: String): User = User()
        override suspend fun signInWithEmailAndPassword(email: String, password: String): User? = null
        override suspend fun isEmailVerified(): Boolean = true
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