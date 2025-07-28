package com.ragl.divide.domain.usecases.eventPayment

import com.ragl.divide.data.models.EventPayment
import com.ragl.divide.data.models.Event
import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.User
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserState
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.utils.Strings
import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeleteEventPaymentUseCaseTest {

    private val mockGroupRepository = MockGroupRepository()
    private val mockUserStateHolder = MockUserStateHolder()
    private val useCase = DeleteEventPaymentUseCase(mockGroupRepository, mockUserStateHolder)

    @Test
    fun `should return success when event payment is deleted successfully`() = runTest {
        // Given
        val groupId = "group123"
        val eventPayment = EventPayment(
            id = "payment123",
            from = "user1",
            to = "user2",
            amount = 50.0,
            eventId = "event123"
        )

        // When
        val result = useCase(groupId, eventPayment)

        // Then
        assertTrue(result is DeleteEventPaymentUseCase.Result.Success)
        assertEquals(groupId, mockGroupRepository.receivedGroupId)
        assertEquals(eventPayment, mockGroupRepository.receivedEventPayment)
        assertTrue(mockUserStateHolder.deleteEventPaymentCalled)
        assertEquals(groupId, mockUserStateHolder.receivedGroupId)
        assertEquals(eventPayment, mockUserStateHolder.receivedEventPayment)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val eventPayment = EventPayment(
            id = "payment123",
            from = "user1",
            to = "user2",
            amount = 50.0,
            eventId = "event123"
        )
        val expectedException = Exception("Database error")
        mockGroupRepository.shouldThrowException = expectedException

        // When
        val result = useCase(groupId, eventPayment)

        // Then
        assertTrue(result is DeleteEventPaymentUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        assertTrue(!mockUserStateHolder.deleteEventPaymentCalled)
    }

    private class MockGroupRepository : GroupRepository {
        var shouldThrowException: Exception? = null
        var receivedGroupId: String? = null
        var receivedEventPayment: EventPayment? = null

        override suspend fun deleteEventPayment(groupId: String, payment: EventPayment) {
            shouldThrowException?.let { throw it }
            receivedGroupId = groupId
            receivedEventPayment = payment
        }

        // Implementaciones vacías para otros métodos no utilizados en el test
        override suspend fun getGroups(groupIds: Map<String, String>): Map<String, Group> = emptyMap()
        override suspend fun getGroup(id: String): Group = Group()
        override suspend fun saveGroup(group: Group, photo: File?): Group = group
        override suspend fun uploadPhoto(photo: File, id: String): String = ""
        override suspend fun getPhoto(id: String): String = ""
        override suspend fun addUser(groupId: String, userId: String) {}
        override suspend fun getUsers(userIds: Collection<String>): List<User> = emptyList()
        override suspend fun leaveGroup(groupId: String) {}
        override suspend fun deleteGroup(groupId: String, image: String) {}
        override suspend fun saveEventExpense(groupId: String, expense: EventExpense): EventExpense = expense
        override suspend fun deleteEventExpense(groupId: String, expense: EventExpense) {}
        override suspend fun saveEventPayment(groupId: String, payment: EventPayment): EventPayment = payment
        override suspend fun saveEvent(groupId: String, event: Event): Event = event
        override suspend fun deleteEvent(groupId: String, eventId: String) {}
        override suspend fun getEvent(groupId: String, eventId: String): Event = Event()
        override suspend fun getEvents(groupId: String): Map<String, Event> = emptyMap()
        override suspend fun settleEvent(groupId: String, eventId: String) {}
        override suspend fun reopenEvent(groupId: String, eventId: String) {}
        override suspend fun saveRecurringExpense(groupId: String, expense: EventExpense): EventExpense = expense
        override suspend fun updateRecurringExpense(groupId: String, expense: EventExpense): EventExpense = expense
        override suspend fun deleteRecurringExpense(groupId: String, expenseId: String) {}
    }

    private class MockUserStateHolder : UserStateHolder {
        var deleteEventPaymentCalled = false
        var receivedGroupId: String? = null
        var receivedEventPayment: EventPayment? = null
        private val _userState = MutableStateFlow(UserState())

        override val userState: StateFlow<UserState> = _userState

        override fun deleteEventPayment(groupId: String, payment: EventPayment) {
            deleteEventPaymentCalled = true
            receivedGroupId = groupId
            receivedEventPayment = payment
        }

        // Implementaciones vacías para otros métodos no utilizados en el test
        override fun refreshUser() {}
        override fun updateUserState(userState: UserState) {}
        override fun updateUser(user: User) {}
        override fun updateGroups(groups: Map<String, Group>) {}
        override fun updateGroupMembers(groupMembers: Map<String, List<com.ragl.divide.data.models.UserInfo>>) {}
        override fun updateFriends(friends: Map<String, com.ragl.divide.data.models.UserInfo>) {}
        override fun updateFriendRequestsReceived(friendRequestsReceived: Map<String, com.ragl.divide.data.models.UserInfo>) {}
        override fun updateFriendRequestsSent(friendRequestsSent: Map<String, com.ragl.divide.data.models.UserInfo>) {}
        override fun removeExpense(expenseId: String) {}
        override fun deleteGroup(groupId: String) {}
        override fun saveGroup(group: Group) {}
        override fun saveExpense(expense: com.ragl.divide.data.models.Expense) {}
        override fun savePayment(expenseId: String, payment: com.ragl.divide.data.models.Payment) {}
        override fun deletePayment(expenseId: String, paymentId: String) {}
        override fun saveEventExpense(groupId: String, expense: EventExpense) {}
        override fun deleteEventExpense(groupId: String, expense: EventExpense) {}
        override fun getExpenseById(expenseId: String): com.ragl.divide.data.models.Expense = com.ragl.divide.data.models.Expense()
        override fun getGroupById(id: String): Group = Group()
        override fun getEventExpenseById(groupId: String, expenseId: String?, eventId: String): EventExpense = EventExpense()
        override fun getEventPaymentById(groupId: String, paymentId: String?, eventId: String): EventPayment = EventPayment()
        override fun getUUID(): String = ""
        override fun saveEventPayment(groupId: String, savedPayment: EventPayment) {}
        override fun updateProfileImage(imagePath: String) {}
        override fun updateUserName(newName: String) {}
        override fun sendFriendRequest(friend: com.ragl.divide.data.models.UserInfo) {}
        override fun acceptFriendRequest(friend: com.ragl.divide.data.models.UserInfo) {}
        override fun rejectFriendRequest(friend: com.ragl.divide.data.models.UserInfo) {}
        override fun cancelFriendRequest(friend: com.ragl.divide.data.models.UserInfo) {}
        override fun removeFriend(friend: com.ragl.divide.data.models.UserInfo) {}
        override fun getGroupMembers(groupId: String): List<com.ragl.divide.data.models.UserInfo> = emptyList()
        override fun getGroupMembersWithGuests(groupId: String): List<com.ragl.divide.data.models.UserInfo> = emptyList()
        override fun setGroupMembers(group: Group, userInfo: List<com.ragl.divide.data.models.UserInfo>) {}
        override fun getEventById(groupId: String, eventId: String?): Event = Event()
        override fun saveEvent(groupId: String, event: Event) {}
        override fun deleteEvent(groupId: String, eventId: String) {}
        override fun settleEvent(groupId: String, eventId: String) {}
        override fun reopenEvent(groupId: String, eventId: String) {}
        override fun recalculateEventDebts(groupId: String, eventId: String) {}
        override fun updateGroupInState(groupId: String, updatedGroup: Group) {}
        override fun updateEventInState(groupId: String, eventId: String, updatedEvent: Event) {}
    }
} 