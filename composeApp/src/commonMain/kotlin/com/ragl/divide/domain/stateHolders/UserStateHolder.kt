package com.ragl.divide.domain.stateHolders

import com.ragl.divide.data.models.Event
import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.data.models.EventPayment
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import com.ragl.divide.data.models.UserInfo
import kotlinx.coroutines.flow.StateFlow

data class UserState(
    val user: User = User(),
    val groups: Map<String, Group> = emptyMap(),
    val groupMembers: Map<String, List<UserInfo>> = emptyMap(),
    val friends: Map<String, UserInfo> = emptyMap(),
    val friendRequestsReceived: Map<String, UserInfo> = emptyMap(),
    val friendRequestsSent: Map<String, UserInfo> = emptyMap()
)

interface UserStateHolder {
    val userState: StateFlow<UserState>
    fun refreshUser()
    fun updateUserState(userState: UserState)
    fun updateUser(user: User)
    fun updateGroups(groups: Map<String, Group>)
    fun updateGroupMembers(groupMembers: Map<String, List<UserInfo>>)
    fun updateFriends(friends: Map<String, UserInfo>)
    fun updateFriendRequestsReceived(friendRequestsReceived: Map<String, UserInfo>)
    fun updateFriendRequestsSent(friendRequestsSent: Map<String, UserInfo>)
    fun removeExpense(expenseId: String)
    fun deleteGroup(groupId: String)
    fun saveGroup(group: Group)
    fun saveExpense(expense: Expense)
    fun savePayment(expenseId: String, payment: Payment)
    fun deletePayment(expenseId: String, paymentId: String)
    fun saveEventExpense(groupId: String, expense: EventExpense)
    fun deleteEventExpense(groupId: String, expense: EventExpense)
    fun getExpenseById(expenseId: String): Expense
    fun getGroupById(id: String): Group
    fun getEventExpenseById(groupId: String, expenseId: String?, eventId: String): EventExpense
    fun getEventPaymentById(groupId: String, paymentId: String?, eventId: String): EventPayment
    fun getUUID(): String
    fun saveEventPayment(groupId: String, savedPayment: EventPayment)
    fun deleteEventPayment(groupId: String, payment: EventPayment)
    fun updateProfileImage(imagePath: String)
    fun updateUserName(newName: String)
    fun sendFriendRequest(friend: UserInfo)
    fun acceptFriendRequest(friend: UserInfo)
    fun rejectFriendRequest(friend: UserInfo)
    fun cancelFriendRequest(friend: UserInfo)
    fun removeFriend(friend: UserInfo)
    fun getGroupMembers(groupId: String): List<UserInfo>
    fun getGroupMembersWithGuests(groupId: String): List<UserInfo>
    fun setGroupMembers(group: Group, userInfo: List<UserInfo>)
    fun getEventById(groupId: String, eventId: String?): Event
    fun saveEvent(groupId: String, event: Event)
    fun deleteEvent(groupId: String, eventId: String)
    fun settleEvent(groupId: String, eventId: String)
    fun reopenEvent(groupId: String, eventId: String)
    fun recalculateEventDebts(groupId: String, eventId: String)
    fun updateGroupInState(groupId: String, updatedGroup: Group)
    fun updateEventInState(groupId: String, eventId: String, updatedEvent: Event)
}