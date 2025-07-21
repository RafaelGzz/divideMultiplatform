package com.ragl.divide.domain.usecases.event

import com.ragl.divide.data.models.Category
import com.ragl.divide.data.models.Event
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import dev.gitlive.firebase.storage.File
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.data.models.EventPayment
import com.ragl.divide.data.models.User
import com.ragl.divide.data.models.UserInfo
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import com.ragl.divide.domain.stateHolders.UserState
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Payment

class SaveEventUseCaseTest {

    private val mockGroupRepository = MockGroupRepository()
    private val mockUserStateHolder = MockUserStateHolder()
    private val useCase = SaveEventUseCase(mockGroupRepository, mockUserStateHolder)

    @Test
    fun `should return success when event is saved successfully`() = runTest {
        // Given
        val groupId = "test_group_id"
        val event = createTestEvent()
        val savedEvent = event.copy(id = "saved_event_id")
        mockGroupRepository.shouldReturnEvent = savedEvent

        // When
        val result = useCase(groupId, event)

        // Then
        assertTrue(result is SaveEventUseCase.Result.Success)
        assertEquals(savedEvent, result.event)
        assertTrue(mockUserStateHolder.saveEventCalled)
        assertEquals(groupId, mockUserStateHolder.receivedGroupId)
        assertEquals(savedEvent, mockUserStateHolder.savedEvent)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "test_group_id"
        val event = createTestEvent()
        val expectedException = Exception("Database error")
        mockGroupRepository.shouldThrowException = expectedException

        // When
        val result = useCase(groupId, event)

        // Then
        assertTrue(result is SaveEventUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        assertTrue(!mockUserStateHolder.saveEventCalled)
    }

    @Test
    fun `should pass correct parameters to repository`() = runTest {
        // Given
        val groupId = "test_group_id"
        val event = createTestEvent()
        val savedEvent = event.copy(id = "saved_event_id")
        mockGroupRepository.shouldReturnEvent = savedEvent

        // When
        useCase(groupId, event)

        // Then
        assertEquals(groupId, mockGroupRepository.receivedGroupId)
        assertEquals(event, mockGroupRepository.receivedEvent)
    }

    private fun createTestEvent() = Event(
        id = "test_event_id",
        title = "Test Event",
        description = "Test event description",
        category = Category.ENTERTAINMENT,
        settled = false
    )

    private class MockGroupRepository : GroupRepository {
        var shouldReturnEvent: Event? = null
        var shouldThrowException: Exception? = null
        var receivedGroupId: String? = null
        var receivedEvent: Event? = null
        
        override suspend fun saveEvent(groupId: String, event: Event): Event {
            shouldThrowException?.let { throw it }
            receivedGroupId = groupId
            receivedEvent = event
            return shouldReturnEvent ?: event
        }
        
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
        override suspend fun deleteEventPayment(groupId: String, payment: EventPayment) {}

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
        var saveEventCalled = false
        var savedEvent: Event? = null
        var receivedGroupId: String? = null
        
        override fun saveEvent(groupId: String, event: Event) {
            saveEventCalled = true
            receivedGroupId = groupId
            savedEvent = event
        }
        
        override val userState: StateFlow<UserState> = MutableStateFlow(UserState())
        override fun refreshUser() {}
        override fun updateUserState(userState: UserState) {}
        override fun updateUser(user: User) {}
        override fun updateGroups(groups: Map<String, Group>) {}
        override fun updateGroupMembers(groupMembers: Map<String, List<UserInfo>>) {}
        override fun updateFriends(friends: Map<String, UserInfo>) {}
        override fun updateFriendRequestsReceived(friendRequestsReceived: Map<String, UserInfo>) {}
        override fun updateFriendRequestsSent(friendRequestsSent: Map<String, UserInfo>) {}
        override fun removeExpense(expenseId: String) {}
        override fun deleteGroup(groupId: String) {}
        override fun saveGroup(group: Group) {}
        override fun saveExpense(expense: Expense) {}
        override fun savePayment(expenseId: String, payment: Payment) {}
        override fun deletePayment(expenseId: String, paymentId: String) {}
        override fun saveEventExpense(groupId: String, expense: EventExpense) {}
        override fun deleteEventExpense(groupId: String, expense: EventExpense) {}
        override fun getExpenseById(expenseId: String): Expense = Expense()
        override fun getGroupById(id: String): Group = Group()
        override fun getEventExpenseById(groupId: String, expenseId: String?, eventId: String): EventExpense = EventExpense()
        override fun getEventPaymentById(groupId: String, paymentId: String?, eventId: String): EventPayment = EventPayment()
        override fun getUUID(): String = "test-uuid"
        override fun saveEventPayment(groupId: String, savedPayment: EventPayment) {}
        override fun deleteEventPayment(groupId: String, payment: EventPayment) {}
        override fun updateProfileImage(imagePath: String) {}
        override fun updateUserName(newName: String) {}
        override fun sendFriendRequest(friend: UserInfo) {}
        override fun acceptFriendRequest(friend: UserInfo) {}
        override fun rejectFriendRequest(friend: UserInfo) {}
        override fun cancelFriendRequest(friend: UserInfo) {}
        override fun removeFriend(friend: UserInfo) {}
        override fun getGroupMembers(groupId: String): List<UserInfo> = emptyList()
        override fun getGroupMembersWithGuests(groupId: String): List<UserInfo> = emptyList()
        override fun setGroupMembers(group: Group, userInfo: List<UserInfo>) {}
        override fun getEventById(groupId: String, eventId: String?): Event = Event()
        override fun deleteEvent(groupId: String, eventId: String) {}
        override fun settleEvent(groupId: String, eventId: String) {}
        override fun reopenEvent(groupId: String, eventId: String) {}
        override fun recalculateEventDebts(groupId: String, eventId: String) {}
        override fun updateGroupInState(groupId: String, updatedGroup: Group) {}
        override fun updateEventInState(groupId: String, eventId: String, updatedEvent: Event) {}
    }
} 