package com.ragl.divide.domain.usecases.payment

import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.stateHolders.UserState
import com.ragl.divide.domain.stateHolders.UserStateHolder
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SaveExpensePaymentUseCaseTest {

    private val mockUserRepository = MockUserRepository()
    private val mockUserStateHolder = MockUserStateHolder()
    private val useCase = SaveExpensePaymentUseCase(mockUserRepository, mockUserStateHolder)

    @Test
    fun `should return success when payment is saved and expense not paid`() = runTest {
        // Given
        val expense = Expense(
            id = "expense123",
            title = "Test Expense",
            amount = 100.0,
            paid = false
        )

        // When
        val result = useCase(expense)

        // Then
        assertTrue(result is SaveExpensePaymentUseCase.Result.Success)
        assertEquals(expense, mockUserRepository.receivedExpense)
        assertTrue(mockUserStateHolder.saveExpenseCalled)
        assertEquals(expense, mockUserStateHolder.savedExpense)
    }

    @Test
    fun `should return paid when payment is saved and expense becomes paid`() = runTest {
        // Given
        val expense = Expense(
            id = "expense123",
            title = "Test Expense",
            amount = 100.0,
            paid = true
        )

        // When
        val result = useCase(expense)

        // Then
        assertTrue(result is SaveExpensePaymentUseCase.Result.Paid)
        assertEquals(expense, mockUserRepository.receivedExpense)
        assertTrue(mockUserStateHolder.saveExpenseCalled)
        assertEquals(expense, mockUserStateHolder.savedExpense)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val expense = Expense(
            id = "expense123",
            title = "Test Expense",
            amount = 100.0,
            paid = false
        )
        val expectedException = Exception("Database error")
        mockUserRepository.shouldThrowException = expectedException

        // When
        val result = useCase(expense)

        // Then
        assertTrue(result is SaveExpensePaymentUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        assertTrue(!mockUserStateHolder.saveExpenseCalled)
    }

    private class MockUserRepository : UserRepository {
        var shouldThrowException: Exception? = null
        var receivedExpense: Expense? = null

        override suspend fun saveExpense(expense: Expense): Expense {
            shouldThrowException?.let { throw it }
            receivedExpense = expense
            return expense
        }

        // Implementaciones vacías para otros métodos no utilizados en el test
        override fun getCurrentUser(): FirebaseUser? = null
        override suspend fun createUserInDatabase(): User = User()
        override suspend fun getUser(id: String): User = User()
        override suspend fun signInWithEmailAndPassword(email: String, password: String): User? = null
        override suspend fun signUpWithEmailAndPassword(email: String, password: String, name: String): User? = null
        override suspend fun signOut() {}
        override suspend fun getExpense(id: String): Expense = Expense()
        override suspend fun getExpenses(): Map<String, Expense> = emptyMap()
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

    private class MockUserStateHolder : UserStateHolder {
        var saveExpenseCalled = false
        var savedExpense: Expense? = null
        private val _userState = MutableStateFlow(UserState())

        override val userState: StateFlow<UserState> = _userState

        override fun saveExpense(expense: Expense) {
            saveExpenseCalled = true
            savedExpense = expense
        }

        // Implementaciones vacías para otros métodos no utilizados en el test
        override fun refreshUser() {}
        override fun updateUserState(userState: UserState) {}
        override fun updateUser(user: User) {}
        override fun updateGroups(groups: Map<String, com.ragl.divide.data.models.Group>) {}
        override fun updateGroupMembers(groupMembers: Map<String, List<com.ragl.divide.data.models.UserInfo>>) {}
        override fun updateFriends(friends: Map<String, com.ragl.divide.data.models.UserInfo>) {}
        override fun updateFriendRequestsReceived(friendRequestsReceived: Map<String, com.ragl.divide.data.models.UserInfo>) {}
        override fun updateFriendRequestsSent(friendRequestsSent: Map<String, com.ragl.divide.data.models.UserInfo>) {}
        override fun removeExpense(expenseId: String) {}
        override fun deleteGroup(groupId: String) {}
        override fun saveGroup(group: com.ragl.divide.data.models.Group) {}
        override fun savePayment(expenseId: String, payment: Payment) {}
        override fun deletePayment(expenseId: String, paymentId: String) {}
        override fun saveEventExpense(groupId: String, expense: com.ragl.divide.data.models.EventExpense) {}
        override fun deleteEventExpense(groupId: String, expense: com.ragl.divide.data.models.EventExpense) {}
        override fun getExpenseById(expenseId: String): Expense = Expense()
        override fun getGroupById(id: String): com.ragl.divide.data.models.Group = com.ragl.divide.data.models.Group()
        override fun getEventExpenseById(groupId: String, expenseId: String?, eventId: String): com.ragl.divide.data.models.EventExpense = com.ragl.divide.data.models.EventExpense()
        override fun getEventPaymentById(groupId: String, paymentId: String?, eventId: String): com.ragl.divide.data.models.EventPayment = com.ragl.divide.data.models.EventPayment()
        override fun getUUID(): String = ""
        override fun saveEventPayment(groupId: String, savedPayment: com.ragl.divide.data.models.EventPayment) {}
        override fun deleteEventPayment(groupId: String, payment: com.ragl.divide.data.models.EventPayment) {}
        override fun updateProfileImage(imagePath: String) {}
        override fun updateUserName(newName: String) {}
        override fun sendFriendRequest(friend: com.ragl.divide.data.models.UserInfo) {}
        override fun acceptFriendRequest(friend: com.ragl.divide.data.models.UserInfo) {}
        override fun rejectFriendRequest(friend: com.ragl.divide.data.models.UserInfo) {}
        override fun cancelFriendRequest(friend: com.ragl.divide.data.models.UserInfo) {}
        override fun removeFriend(friend: com.ragl.divide.data.models.UserInfo) {}
        override fun getGroupMembers(groupId: String): List<com.ragl.divide.data.models.UserInfo> = emptyList()
        override fun getGroupMembersWithGuests(groupId: String): List<com.ragl.divide.data.models.UserInfo> = emptyList()
        override fun setGroupMembers(group: com.ragl.divide.data.models.Group, userInfo: List<com.ragl.divide.data.models.UserInfo>) {}
        override fun getEventById(groupId: String, eventId: String?): com.ragl.divide.data.models.Event = com.ragl.divide.data.models.Event()
        override fun saveEvent(groupId: String, event: com.ragl.divide.data.models.Event) {}
        override fun deleteEvent(groupId: String, eventId: String) {}
        override fun settleEvent(groupId: String, eventId: String) {}
        override fun reopenEvent(groupId: String, eventId: String) {}
        override fun recalculateEventDebts(groupId: String, eventId: String) {}
        override fun updateGroupInState(groupId: String, updatedGroup: com.ragl.divide.data.models.Group) {}
        override fun updateEventInState(groupId: String, eventId: String, updatedEvent: com.ragl.divide.data.models.Event) {}
    }
} 