package com.ragl.divide.domain.usecases.group

import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.Event
import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.data.models.EventPayment
import com.ragl.divide.data.models.User
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.repositories.FriendsRepository
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserState
import com.ragl.divide.domain.stateHolders.UserStateHolder
import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SaveGroupUseCaseTest {

    private val mockGroupRepository = MockGroupRepository()
    private val mockFriendsRepository = MockFriendsRepository()
    private val mockUserStateHolder = MockUserStateHolder()
    private val useCase = SaveGroupUseCase(mockGroupRepository, mockFriendsRepository, mockUserStateHolder)

    @Test
    fun `should return success when group is saved successfully`() = runTest {
        // Given
        val group = Group(
            id = "group123",
            name = "Test Group",
            users = mapOf("user1" to "user1", "user2" to "user2")
        )
        val imageFile = null as File?
        val savedGroup = group.copy(name = "Saved Test Group")
        mockGroupRepository.shouldReturnGroup = savedGroup
        
        val currentMembers = listOf(UserInfo(uuid = "user1", name = "User 1"))
        mockUserStateHolder.mockGroupMembers = currentMembers
        
        val newFriends = mapOf("user2" to UserInfo(uuid = "user2", name = "User 2"))
        mockFriendsRepository.shouldReturnFriends = newFriends

        // When
        val result = useCase(group, imageFile)

        // Then
        assertTrue(result is SaveGroupUseCase.Result.Success)
        assertEquals(group, mockGroupRepository.receivedGroup)
        assertEquals(imageFile, mockGroupRepository.receivedImageFile)
        assertTrue(mockUserStateHolder.saveGroupCalled)
        assertEquals(savedGroup, mockUserStateHolder.savedGroup)
        assertTrue(mockUserStateHolder.setGroupMembersCalled)
    }

    @Test
    fun `should not fetch new friends when all users are already in group members`() = runTest {
        // Given
        val group = Group(
            id = "group123",
            name = "Test Group",
            users = mapOf("user1" to "user1", "user2" to "user2")
        )
        val imageFile = null as File?
        val savedGroup = group.copy(name = "Saved Test Group")
        mockGroupRepository.shouldReturnGroup = savedGroup
        
        val currentMembers = listOf(
            UserInfo(uuid = "user1", name = "User 1"),
            UserInfo(uuid = "user2", name = "User 2")
        )
        mockUserStateHolder.mockGroupMembers = currentMembers

        // When
        val result = useCase(group, imageFile)

        // Then
        assertTrue(result is SaveGroupUseCase.Result.Success)
        assertTrue(!mockFriendsRepository.getFriendsCalled)
        assertTrue(!mockUserStateHolder.setGroupMembersCalled)
        assertTrue(mockUserStateHolder.saveGroupCalled)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val group = Group(
            id = "group123",
            name = "Test Group",
            users = mapOf("user1" to "user1")
        )
        val imageFile = null as File?
        val expectedException = Exception("Database error")
        mockGroupRepository.shouldThrowException = expectedException

        // When
        val result = useCase(group, imageFile)

        // Then
        assertTrue(result is SaveGroupUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        assertTrue(!mockUserStateHolder.saveGroupCalled)
    }

    private class MockGroupRepository : GroupRepository {
        var shouldReturnGroup: Group? = null
        var shouldThrowException: Exception? = null
        var receivedGroup: Group? = null
        var receivedImageFile: File? = null

        override suspend fun saveGroup(group: Group, photo: File?): Group {
            shouldThrowException?.let { throw it }
            receivedGroup = group
            receivedImageFile = photo
            return shouldReturnGroup ?: group
        }

        // Implementaciones vacías para otros métodos no utilizados en el test
        override suspend fun getGroups(groupIds: Map<String, String>): Map<String, Group> = emptyMap()
        override suspend fun getGroup(id: String): Group = Group()
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

    private class MockFriendsRepository : FriendsRepository {
        var shouldReturnFriends: Map<String, UserInfo> = emptyMap()
        var getFriendsCalled = false
        var receivedFriendIds: List<String>? = null

        override suspend fun getFriends(friends: List<String>): Map<String, UserInfo> {
            getFriendsCalled = true
            receivedFriendIds = friends
            return shouldReturnFriends
        }

        // Implementaciones vacías para otros métodos no utilizados en el test
        override suspend fun getGroupMembers(userIds: List<String>, localUsers: Map<String, UserInfo>): List<UserInfo> = emptyList()
        override suspend fun searchUsers(query: String, existing: List<UserInfo>): Map<String, UserInfo> = emptyMap()
        override suspend fun addFriend(friend: UserInfo): UserInfo = friend
        override suspend fun sendFriendRequest(friendId: String): Boolean = false
        override suspend fun acceptFriendRequest(friendId: String): Boolean = false
        override suspend fun cancelFriendRequest(friendId: String): Boolean = false
        override suspend fun rejectFriendRequest(friendId: String): Boolean = false
        override suspend fun removeFriend(friendId: String): Boolean = false
        override suspend fun getFriendRequestsReceived(requests: Map<String, String>): Map<String, UserInfo> = emptyMap()
        override suspend fun getFriendRequestsSent(requests: Map<String, String>): Map<String, UserInfo> = emptyMap()
    }

    private class MockUserStateHolder : UserStateHolder {
        var saveGroupCalled = false
        var setGroupMembersCalled = false
        var savedGroup: Group? = null
        var mockGroupMembers: List<UserInfo> = emptyList()
        private val _userState = MutableStateFlow(UserState())

        override val userState: StateFlow<UserState> = _userState

        override fun saveGroup(group: Group) {
            saveGroupCalled = true
            savedGroup = group
        }

        override fun getGroupMembers(groupId: String): List<UserInfo> {
            return mockGroupMembers
        }

        override fun setGroupMembers(group: Group, userInfo: List<UserInfo>) {
            setGroupMembersCalled = true
        }

        // Implementaciones vacías para otros métodos no utilizados en el test
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
        override fun deleteEventPayment(groupId: String, payment: EventPayment) {}
        override fun updateProfileImage(imagePath: String) {}
        override fun updateUserName(newName: String) {}
        override fun sendFriendRequest(friend: UserInfo) {}
        override fun acceptFriendRequest(friend: UserInfo) {}
        override fun rejectFriendRequest(friend: UserInfo) {}
        override fun cancelFriendRequest(friend: UserInfo) {}
        override fun removeFriend(friend: UserInfo) {}
        override fun getGroupMembersWithGuests(groupId: String): List<UserInfo> = emptyList()
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