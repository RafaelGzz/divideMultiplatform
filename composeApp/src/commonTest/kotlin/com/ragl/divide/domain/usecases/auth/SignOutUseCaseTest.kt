package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.ScheduleNotificationService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import dev.gitlive.firebase.auth.FirebaseUser
import com.ragl.divide.data.models.User
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Frequency
import com.ragl.divide.data.models.Payment
import dev.gitlive.firebase.storage.File

class SignOutUseCaseTest {

    private val mockUserRepository = MockUserRepository()
    private val mockScheduleNotificationService = MockScheduleNotificationService()
    private val useCase = SignOutUseCase(mockUserRepository, mockScheduleNotificationService)

    @Test
    fun `should return success when signout is successful`() = runTest {
        // Given
        mockUserRepository.shouldReturnFirebaseUser = null

        // When
        val result = useCase()

        // Then
        assertTrue(result is SignOutUseCase.Result.Success)
        assertTrue(mockScheduleNotificationService.cancelAllNotificationsCalled)
        assertTrue(mockUserRepository.signOutCalled)
    }



    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val expectedException = Exception("Network error")
        mockUserRepository.shouldThrowException = expectedException

        // When
        val result = useCase()

        // Then
        assertTrue(result is SignOutUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        assertTrue(mockScheduleNotificationService.cancelAllNotificationsCalled)
    }

    @Test
    fun `should cancel all notifications before signing out`() = runTest {
        // Given
        mockUserRepository.shouldReturnFirebaseUser = null

        // When
        useCase()

        // Then
        assertTrue(mockScheduleNotificationService.cancelAllNotificationsCalled)
        assertTrue(mockUserRepository.signOutCalled)
    }

    private class MockUserRepository : UserRepository {
        var shouldReturnFirebaseUser: FirebaseUser? = null
        var shouldThrowException: Exception? = null
        var signOutCalled = false

        override suspend fun signOut() {
            shouldThrowException?.let { throw it }
            signOutCalled = true
        }

        override fun getCurrentUser(): FirebaseUser? {
            return shouldReturnFirebaseUser
        }

        // Implementaciones vacías para otros métodos no utilizados en el test
        override suspend fun createUserInDatabase(): User = User()
        override suspend fun getUser(id: String): User = User()
        override suspend fun signInWithEmailAndPassword(email: String, password: String): User? = User()
        override suspend fun signUpWithEmailAndPassword(email: String, password: String, name: String): User? = User()
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
        override suspend fun updateUserName(newName: String): Boolean = true
    }

    private class MockScheduleNotificationService : ScheduleNotificationService {
        var cancelAllNotificationsCalled = false

        override fun cancelAllNotifications() {
            cancelAllNotificationsCalled = true
        }

        // Implementaciones vacías para otros métodos no utilizados en el test
        override fun scheduleNotification(id: Int, title: String, message: String, startingDateMillis: Long, frequency: Frequency, useSound: Boolean) {}
        override fun cancelNotification(id: Int) {}
        override fun canScheduleExactAlarms(): Boolean = true
        override fun requestScheduleExactAlarmPermission() {}
        override fun hasNotificationPermission(): Boolean = true
        override fun requestNotificationPermission() {}
        override fun wasNotificationPermissionRejectedPermanently(): Boolean = false
    }
} 