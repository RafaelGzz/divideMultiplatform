package com.ragl.divide.testing

import com.ragl.divide.data.models.Event
import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.data.models.EventPayment
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Frequency
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.repositories.FriendsRepository
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.AnalyticsService
import com.ragl.divide.domain.services.ScheduleNotificationService
import com.ragl.divide.domain.stateHolders.UserState
import com.ragl.divide.domain.stateHolders.UserStateHolder
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class FakeInvocation(
    val method: String,
    val arguments: List<Any?>
)

abstract class FakeCallTracker {
    val invocations = mutableListOf<FakeInvocation>()

    fun callCount(method: String): Int = invocations.count { it.method == method }

    protected fun record(method: String, vararg arguments: Any?) {
        invocations += FakeInvocation(method, arguments.toList())
    }
}

fun assertCalled(fake: FakeCallTracker, method: String, vararg arguments: Any?) {
    val invocation = fake.invocations.lastOrNull { it.method == method }
        ?: error("Expected $method to be called")
    check(invocation.arguments == arguments.toList()) {
        "Expected $method(${arguments.toList()}), got ${invocation.arguments}"
    }
}

fun assertNotCalled(fake: FakeCallTracker, method: String) {
    check(fake.callCount(method) == 0) { "Expected $method not to be called" }
}

private fun unconfigured(method: String): Nothing =
    error("No behavior configured for $method")

class FakeUserRepository : FakeCallTracker(), UserRepository {
    var onGetCurrentUser: () -> FirebaseUser? = { unconfigured("getCurrentUser") }
    var onCreateUserInDatabase: suspend () -> User = { unconfigured("createUserInDatabase") }
    var onGetUser: suspend (String) -> User = { unconfigured("getUser") }
    var onSignInWithEmailAndPassword: suspend (String, String) -> User? = { _, _ ->
        unconfigured("signInWithEmailAndPassword")
    }
    var onSignUpWithEmailAndPassword: suspend (String, String, String) -> User? = { _, _, _ ->
        unconfigured("signUpWithEmailAndPassword")
    }
    var onSignOut: suspend () -> Unit = { unconfigured("signOut") }
    var onGetExpense: suspend (String) -> Expense = { unconfigured("getExpense") }
    var onGetExpenses: suspend () -> Map<String, Expense> = { unconfigured("getExpenses") }
    var onSaveExpense: suspend (Expense) -> Expense = { unconfigured("saveExpense") }
    var onDeleteExpense: suspend (String) -> Unit = { unconfigured("deleteExpense") }
    var onGetExpensePayments: suspend (String) -> Map<String, Payment> = { unconfigured("getExpensePayments") }
    var onSaveExpensePayment: suspend (Payment, String, Boolean) -> Payment = { _, _, _ ->
        unconfigured("saveExpensePayment")
    }
    var onDeleteExpensePayment: suspend (String, Double, String) -> Unit = { _, _, _ ->
        unconfigured("deleteExpensePayment")
    }
    var onAddGroupToUser: suspend (String, String) -> Unit = { _, _ -> unconfigured("addGroupToUser") }
    var onRemoveGroupFromUser: suspend (String, String) -> Unit = { _, _ -> unconfigured("removeGroupFromUser") }
    var onSendEmailVerification: suspend () -> Unit = { unconfigured("sendEmailVerification") }
    var onIsEmailVerified: suspend () -> Boolean = { unconfigured("isEmailVerified") }
    var onSaveProfilePhoto: suspend (File) -> String = { unconfigured("saveProfilePhoto") }
    var onGetProfilePhoto: suspend (String) -> String = { unconfigured("getProfilePhoto") }
    var onUpdateUserName: suspend (String) -> Boolean = { unconfigured("updateUserName") }

    override fun getCurrentUser(): FirebaseUser? {
        record("getCurrentUser")
        return onGetCurrentUser()
    }

    override suspend fun createUserInDatabase(): User {
        record("createUserInDatabase")
        return onCreateUserInDatabase()
    }

    override suspend fun getUser(id: String): User {
        record("getUser", id)
        return onGetUser(id)
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): User? {
        record("signInWithEmailAndPassword", email, password)
        return onSignInWithEmailAndPassword(email, password)
    }

    override suspend fun signUpWithEmailAndPassword(email: String, password: String, name: String): User? {
        record("signUpWithEmailAndPassword", email, password, name)
        return onSignUpWithEmailAndPassword(email, password, name)
    }

    override suspend fun signOut() {
        record("signOut")
        onSignOut()
    }

    override suspend fun getExpense(id: String): Expense {
        record("getExpense", id)
        return onGetExpense(id)
    }

    override suspend fun getExpenses(): Map<String, Expense> {
        record("getExpenses")
        return onGetExpenses()
    }

    override suspend fun saveExpense(expense: Expense): Expense {
        record("saveExpense", expense)
        return onSaveExpense(expense)
    }

    override suspend fun deleteExpense(id: String) {
        record("deleteExpense", id)
        onDeleteExpense(id)
    }

    override suspend fun getExpensePayments(expenseId: String): Map<String, Payment> {
        record("getExpensePayments", expenseId)
        return onGetExpensePayments(expenseId)
    }

    override suspend fun saveExpensePayment(payment: Payment, expenseId: String, expensePaid: Boolean): Payment {
        record("saveExpensePayment", payment, expenseId, expensePaid)
        return onSaveExpensePayment(payment, expenseId, expensePaid)
    }

    override suspend fun deleteExpensePayment(paymentId: String, amount: Double, expenseId: String) {
        record("deleteExpensePayment", paymentId, amount, expenseId)
        onDeleteExpensePayment(paymentId, amount, expenseId)
    }

    override suspend fun addGroupToUser(id: String, userId: String) {
        record("addGroupToUser", id, userId)
        onAddGroupToUser(id, userId)
    }

    override suspend fun removeGroupFromUser(groupId: String, userId: String) {
        record("removeGroupFromUser", groupId, userId)
        onRemoveGroupFromUser(groupId, userId)
    }

    override suspend fun sendEmailVerification() {
        record("sendEmailVerification")
        onSendEmailVerification()
    }

    override suspend fun isEmailVerified(): Boolean {
        record("isEmailVerified")
        return onIsEmailVerified()
    }

    override suspend fun saveProfilePhoto(photo: File): String {
        record("saveProfilePhoto", photo)
        return onSaveProfilePhoto(photo)
    }

    override suspend fun getProfilePhoto(userId: String): String {
        record("getProfilePhoto", userId)
        return onGetProfilePhoto(userId)
    }

    override suspend fun updateUserName(newName: String): Boolean {
        record("updateUserName", newName)
        return onUpdateUserName(newName)
    }
}

class FakeGroupRepository : FakeCallTracker(), GroupRepository {
    var onGetGroups: suspend (Map<String, String>) -> Map<String, Group> = { unconfigured("getGroups") }
    var onGetGroup: suspend (String) -> Group = { unconfigured("getGroup") }
    var onSaveGroup: suspend (Group, File?) -> Group = { _, _ -> unconfigured("saveGroup") }
    var onUploadPhoto: suspend (File, String) -> String = { _, _ -> unconfigured("uploadPhoto") }
    var onGetPhoto: suspend (String) -> String = { unconfigured("getPhoto") }
    var onAddUser: suspend (String, String) -> Unit = { _, _ -> unconfigured("addUser") }
    var onGetUsers: suspend (Collection<String>) -> List<User> = { unconfigured("getUsers") }
    var onLeaveGroup: suspend (String) -> Unit = { unconfigured("leaveGroup") }
    var onDeleteGroup: suspend (String, String) -> Unit = { _, _ -> unconfigured("deleteGroup") }
    var onSaveEventExpense: suspend (String, EventExpense) -> EventExpense = { _, _ -> unconfigured("saveEventExpense") }
    var onDeleteEventExpense: suspend (String, EventExpense) -> Unit = { _, _ -> unconfigured("deleteEventExpense") }
    var onSaveEventPayment: suspend (String, EventPayment) -> EventPayment = { _, _ -> unconfigured("saveEventPayment") }
    var onDeleteEventPayment: suspend (String, EventPayment) -> Unit = { _, _ -> unconfigured("deleteEventPayment") }
    var onSaveEvent: suspend (String, Event) -> Event = { _, _ -> unconfigured("saveEvent") }
    var onDeleteEvent: suspend (String, String) -> Unit = { _, _ -> unconfigured("deleteEvent") }
    var onGetEvent: suspend (String, String) -> Event = { _, _ -> unconfigured("getEvent") }
    var onGetEvents: suspend (String) -> Map<String, Event> = { unconfigured("getEvents") }
    var onSettleEvent: suspend (String, String) -> Unit = { _, _ -> unconfigured("settleEvent") }
    var onReopenEvent: suspend (String, String) -> Unit = { _, _ -> unconfigured("reopenEvent") }
    var onSaveRecurringExpense: suspend (String, EventExpense) -> EventExpense = { _, _ -> unconfigured("saveRecurringExpense") }
    var onUpdateRecurringExpense: suspend (String, EventExpense) -> EventExpense = { _, _ -> unconfigured("updateRecurringExpense") }
    var onDeleteRecurringExpense: suspend (String, String) -> Unit = { _, _ -> unconfigured("deleteRecurringExpense") }

    override suspend fun getGroups(groupIds: Map<String, String>): Map<String, Group> {
        record("getGroups", groupIds)
        return onGetGroups(groupIds)
    }
    override suspend fun getGroup(id: String): Group {
        record("getGroup", id)
        return onGetGroup(id)
    }
    override suspend fun saveGroup(group: Group, photo: File?): Group {
        record("saveGroup", group, photo)
        return onSaveGroup(group, photo)
    }
    override suspend fun uploadPhoto(photo: File, id: String): String {
        record("uploadPhoto", photo, id)
        return onUploadPhoto(photo, id)
    }
    override suspend fun getPhoto(id: String): String {
        record("getPhoto", id)
        return onGetPhoto(id)
    }
    override suspend fun addUser(groupId: String, userId: String) {
        record("addUser", groupId, userId)
        onAddUser(groupId, userId)
    }
    override suspend fun getUsers(userIds: Collection<String>): List<User> {
        record("getUsers", userIds)
        return onGetUsers(userIds)
    }
    override suspend fun leaveGroup(groupId: String) {
        record("leaveGroup", groupId)
        onLeaveGroup(groupId)
    }
    override suspend fun deleteGroup(groupId: String, image: String) {
        record("deleteGroup", groupId, image)
        onDeleteGroup(groupId, image)
    }
    override suspend fun saveEventExpense(groupId: String, expense: EventExpense): EventExpense {
        record("saveEventExpense", groupId, expense)
        return onSaveEventExpense(groupId, expense)
    }
    override suspend fun deleteEventExpense(groupId: String, expense: EventExpense) {
        record("deleteEventExpense", groupId, expense)
        onDeleteEventExpense(groupId, expense)
    }
    override suspend fun saveEventPayment(groupId: String, payment: EventPayment): EventPayment {
        record("saveEventPayment", groupId, payment)
        return onSaveEventPayment(groupId, payment)
    }
    override suspend fun deleteEventPayment(groupId: String, payment: EventPayment) {
        record("deleteEventPayment", groupId, payment)
        onDeleteEventPayment(groupId, payment)
    }
    override suspend fun saveEvent(groupId: String, event: Event): Event {
        record("saveEvent", groupId, event)
        return onSaveEvent(groupId, event)
    }
    override suspend fun deleteEvent(groupId: String, eventId: String) {
        record("deleteEvent", groupId, eventId)
        onDeleteEvent(groupId, eventId)
    }
    override suspend fun getEvent(groupId: String, eventId: String): Event {
        record("getEvent", groupId, eventId)
        return onGetEvent(groupId, eventId)
    }
    override suspend fun getEvents(groupId: String): Map<String, Event> {
        record("getEvents", groupId)
        return onGetEvents(groupId)
    }
    override suspend fun settleEvent(groupId: String, eventId: String) {
        record("settleEvent", groupId, eventId)
        onSettleEvent(groupId, eventId)
    }
    override suspend fun reopenEvent(groupId: String, eventId: String) {
        record("reopenEvent", groupId, eventId)
        onReopenEvent(groupId, eventId)
    }
    override suspend fun saveRecurringExpense(groupId: String, expense: EventExpense): EventExpense {
        record("saveRecurringExpense", groupId, expense)
        return onSaveRecurringExpense(groupId, expense)
    }
    override suspend fun updateRecurringExpense(groupId: String, expense: EventExpense): EventExpense {
        record("updateRecurringExpense", groupId, expense)
        return onUpdateRecurringExpense(groupId, expense)
    }
    override suspend fun deleteRecurringExpense(groupId: String, expenseId: String) {
        record("deleteRecurringExpense", groupId, expenseId)
        onDeleteRecurringExpense(groupId, expenseId)
    }
}

class FakeFriendsRepository : FakeCallTracker(), FriendsRepository {
    var onGetFriends: suspend (List<String>) -> Map<String, UserInfo> = { unconfigured("getFriends") }
    var onGetGroupMembers: suspend (List<String>, Map<String, UserInfo>) -> List<UserInfo> = { _, _ -> unconfigured("getGroupMembers") }
    var onSearchUsers: suspend (String, List<UserInfo>) -> Map<String, UserInfo> = { _, _ -> unconfigured("searchUsers") }
    var onAddFriend: suspend (UserInfo) -> UserInfo = { unconfigured("addFriend") }
    var onSendFriendRequest: suspend (String) -> Boolean = { unconfigured("sendFriendRequest") }
    var onAcceptFriendRequest: suspend (String) -> Boolean = { unconfigured("acceptFriendRequest") }
    var onCancelFriendRequest: suspend (String) -> Boolean = { unconfigured("cancelFriendRequest") }
    var onRejectFriendRequest: suspend (String) -> Boolean = { unconfigured("rejectFriendRequest") }
    var onRemoveFriend: suspend (String) -> Boolean = { unconfigured("removeFriend") }
    var onGetFriendRequestsReceived: suspend (Map<String, String>) -> Map<String, UserInfo> = { unconfigured("getFriendRequestsReceived") }
    var onGetFriendRequestsSent: suspend (Map<String, String>) -> Map<String, UserInfo> = { unconfigured("getFriendRequestsSent") }

    override suspend fun getFriends(friends: List<String>): Map<String, UserInfo> {
        record("getFriends", friends)
        return onGetFriends(friends)
    }
    override suspend fun getGroupMembers(userIds: List<String>, localUsers: Map<String, UserInfo>): List<UserInfo> {
        record("getGroupMembers", userIds, localUsers)
        return onGetGroupMembers(userIds, localUsers)
    }
    override suspend fun searchUsers(query: String, existing: List<UserInfo>): Map<String, UserInfo> {
        record("searchUsers", query, existing)
        return onSearchUsers(query, existing)
    }
    override suspend fun addFriend(friend: UserInfo): UserInfo {
        record("addFriend", friend)
        return onAddFriend(friend)
    }
    override suspend fun sendFriendRequest(friendId: String): Boolean {
        record("sendFriendRequest", friendId)
        return onSendFriendRequest(friendId)
    }
    override suspend fun acceptFriendRequest(friendId: String): Boolean {
        record("acceptFriendRequest", friendId)
        return onAcceptFriendRequest(friendId)
    }
    override suspend fun cancelFriendRequest(friendId: String): Boolean {
        record("cancelFriendRequest", friendId)
        return onCancelFriendRequest(friendId)
    }
    override suspend fun rejectFriendRequest(friendId: String): Boolean {
        record("rejectFriendRequest", friendId)
        return onRejectFriendRequest(friendId)
    }
    override suspend fun removeFriend(friendId: String): Boolean {
        record("removeFriend", friendId)
        return onRemoveFriend(friendId)
    }
    override suspend fun getFriendRequestsReceived(requests: Map<String, String>): Map<String, UserInfo> {
        record("getFriendRequestsReceived", requests)
        return onGetFriendRequestsReceived(requests)
    }
    override suspend fun getFriendRequestsSent(requests: Map<String, String>): Map<String, UserInfo> {
        record("getFriendRequestsSent", requests)
        return onGetFriendRequestsSent(requests)
    }
}

class FakeAnalyticsService : FakeCallTracker(), AnalyticsService {
    var onSetUserProperties: (String, String) -> Unit = { _, _ -> unconfigured("setUserProperties") }
    var onLogEvent: (String, Map<String, Any>) -> Unit = { _, _ -> unconfigured("logEvent") }
    var onLogError: (Throwable, String?) -> Unit = { _, _ -> unconfigured("logError") }

    override fun setUserProperties(userId: String, userName: String) {
        record("setUserProperties", userId, userName)
        onSetUserProperties(userId, userName)
    }
    override fun logEvent(eventName: String, params: Map<String, Any>) {
        record("logEvent", eventName, params)
        onLogEvent(eventName, params)
    }
    override fun logError(throwable: Throwable, message: String?) {
        record("logError", throwable, message)
        onLogError(throwable, message)
    }
}

class FakeScheduleNotificationService : FakeCallTracker(), ScheduleNotificationService {
    var onScheduleNotification: (Int, String, String, Long, Frequency, Boolean) -> Unit = { _, _, _, _, _, _ ->
        unconfigured("scheduleNotification")
    }
    var onCanScheduleExactAlarms: () -> Boolean = { unconfigured("canScheduleExactAlarms") }
    var onRequestScheduleExactAlarmPermission: () -> Unit = { unconfigured("requestScheduleExactAlarmPermission") }
    var onCancelNotification: (Int) -> Unit = { unconfigured("cancelNotification") }
    var onCancelAllNotifications: () -> Unit = { unconfigured("cancelAllNotifications") }
    var onHasNotificationPermission: () -> Boolean = { unconfigured("hasNotificationPermission") }
    var onRequestNotificationPermission: () -> Unit = { unconfigured("requestNotificationPermission") }
    var onWasNotificationPermissionRejectedPermanently: () -> Boolean = { unconfigured("wasNotificationPermissionRejectedPermanently") }

    override fun scheduleNotification(id: Int, title: String, message: String, startingDateMillis: Long, frequency: Frequency, useSound: Boolean) {
        record("scheduleNotification", id, title, message, startingDateMillis, frequency, useSound)
        onScheduleNotification(id, title, message, startingDateMillis, frequency, useSound)
    }
    override fun canScheduleExactAlarms(): Boolean {
        record("canScheduleExactAlarms")
        return onCanScheduleExactAlarms()
    }
    override fun requestScheduleExactAlarmPermission() {
        record("requestScheduleExactAlarmPermission")
        onRequestScheduleExactAlarmPermission()
    }
    override fun cancelNotification(id: Int) {
        record("cancelNotification", id)
        onCancelNotification(id)
    }
    override fun cancelAllNotifications() {
        record("cancelAllNotifications")
        onCancelAllNotifications()
    }
    override fun hasNotificationPermission(): Boolean {
        record("hasNotificationPermission")
        return onHasNotificationPermission()
    }
    override fun requestNotificationPermission() {
        record("requestNotificationPermission")
        onRequestNotificationPermission()
    }
    override fun wasNotificationPermissionRejectedPermanently(): Boolean {
        record("wasNotificationPermissionRejectedPermanently")
        return onWasNotificationPermissionRejectedPermanently()
    }
}

class FakeUserStateHolder(initialState: UserState = UserState()) : FakeCallTracker(), UserStateHolder {
    override val userState: StateFlow<UserState> = MutableStateFlow(initialState)
    var onRefreshUser: () -> Unit = { unconfigured("refreshUser") }
    var onUpdateUserState: (UserState) -> Unit = { unconfigured("updateUserState") }
    var onUpdateUser: (User) -> Unit = { unconfigured("updateUser") }
    var onUpdateGroups: (Map<String, Group>) -> Unit = { unconfigured("updateGroups") }
    var onUpdateGroupMembers: (Map<String, List<UserInfo>>) -> Unit = { unconfigured("updateGroupMembers") }
    var onUpdateFriends: (Map<String, UserInfo>) -> Unit = { unconfigured("updateFriends") }
    var onUpdateFriendRequestsReceived: (Map<String, UserInfo>) -> Unit = { unconfigured("updateFriendRequestsReceived") }
    var onUpdateFriendRequestsSent: (Map<String, UserInfo>) -> Unit = { unconfigured("updateFriendRequestsSent") }
    var onRemoveExpense: (String) -> Unit = { unconfigured("removeExpense") }
    var onDeleteGroup: (String) -> Unit = { unconfigured("deleteGroup") }
    var onSaveGroup: (Group) -> Unit = { unconfigured("saveGroup") }
    var onSaveExpense: (Expense) -> Unit = { unconfigured("saveExpense") }
    var onSavePayment: (String, Payment) -> Unit = { _, _ -> unconfigured("savePayment") }
    var onDeletePayment: (String, String) -> Unit = { _, _ -> unconfigured("deletePayment") }
    var onSaveEventExpense: (String, EventExpense) -> Unit = { _, _ -> unconfigured("saveEventExpense") }
    var onDeleteEventExpense: (String, EventExpense) -> Unit = { _, _ -> unconfigured("deleteEventExpense") }
    var onGetExpenseById: (String) -> Expense = { unconfigured("getExpenseById") }
    var onGetGroupById: (String) -> Group = { unconfigured("getGroupById") }
    var onGetEventExpenseById: (String, String?, String) -> EventExpense = { _, _, _ -> unconfigured("getEventExpenseById") }
    var onGetEventPaymentById: (String, String?, String) -> EventPayment = { _, _, _ -> unconfigured("getEventPaymentById") }
    var onGetUUID: () -> String = { unconfigured("getUUID") }
    var onSaveEventPayment: (String, EventPayment) -> Unit = { _, _ -> unconfigured("saveEventPayment") }
    var onDeleteEventPayment: (String, EventPayment) -> Unit = { _, _ -> unconfigured("deleteEventPayment") }
    var onUpdateProfileImage: (String) -> Unit = { unconfigured("updateProfileImage") }
    var onUpdateUserName: (String) -> Unit = { unconfigured("updateUserName") }
    var onSendFriendRequest: (UserInfo) -> Unit = { unconfigured("sendFriendRequest") }
    var onAcceptFriendRequest: (UserInfo) -> Unit = { unconfigured("acceptFriendRequest") }
    var onRejectFriendRequest: (UserInfo) -> Unit = { unconfigured("rejectFriendRequest") }
    var onCancelFriendRequest: (UserInfo) -> Unit = { unconfigured("cancelFriendRequest") }
    var onRemoveFriend: (UserInfo) -> Unit = { unconfigured("removeFriend") }
    var onGetGroupMembers: (String) -> List<UserInfo> = { unconfigured("getGroupMembers") }
    var onGetGroupMembersWithGuests: (String) -> List<UserInfo> = { unconfigured("getGroupMembersWithGuests") }
    var onSetGroupMembers: (Group, List<UserInfo>) -> Unit = { _, _ -> unconfigured("setGroupMembers") }
    var onGetEventById: (String, String?) -> Event = { _, _ -> unconfigured("getEventById") }
    var onSaveEvent: (String, Event) -> Unit = { _, _ -> unconfigured("saveEvent") }
    var onDeleteEvent: (String, String) -> Unit = { _, _ -> unconfigured("deleteEvent") }
    var onSettleEvent: (String, String) -> Unit = { _, _ -> unconfigured("settleEvent") }
    var onReopenEvent: (String, String) -> Unit = { _, _ -> unconfigured("reopenEvent") }
    var onRecalculateEventDebts: (String, String) -> Unit = { _, _ -> unconfigured("recalculateEventDebts") }
    var onUpdateGroupInState: (String, Group) -> Unit = { _, _ -> unconfigured("updateGroupInState") }
    var onUpdateEventInState: (String, String, Event) -> Unit = { _, _, _ -> unconfigured("updateEventInState") }

    override fun refreshUser() { record("refreshUser"); onRefreshUser() }
    override fun updateUserState(userState: UserState) { record("updateUserState", userState); onUpdateUserState(userState) }
    override fun updateUser(user: User) { record("updateUser", user); onUpdateUser(user) }
    override fun updateGroups(groups: Map<String, Group>) { record("updateGroups", groups); onUpdateGroups(groups) }
    override fun updateGroupMembers(groupMembers: Map<String, List<UserInfo>>) { record("updateGroupMembers", groupMembers); onUpdateGroupMembers(groupMembers) }
    override fun updateFriends(friends: Map<String, UserInfo>) { record("updateFriends", friends); onUpdateFriends(friends) }
    override fun updateFriendRequestsReceived(friendRequestsReceived: Map<String, UserInfo>) {
        record("updateFriendRequestsReceived", friendRequestsReceived)
        onUpdateFriendRequestsReceived(friendRequestsReceived)
    }
    override fun updateFriendRequestsSent(friendRequestsSent: Map<String, UserInfo>) {
        record("updateFriendRequestsSent", friendRequestsSent)
        onUpdateFriendRequestsSent(friendRequestsSent)
    }
    override fun removeExpense(expenseId: String) { record("removeExpense", expenseId); onRemoveExpense(expenseId) }
    override fun deleteGroup(groupId: String) { record("deleteGroup", groupId); onDeleteGroup(groupId) }
    override fun saveGroup(group: Group) { record("saveGroup", group); onSaveGroup(group) }
    override fun saveExpense(expense: Expense) { record("saveExpense", expense); onSaveExpense(expense) }
    override fun savePayment(expenseId: String, payment: Payment) { record("savePayment", expenseId, payment); onSavePayment(expenseId, payment) }
    override fun deletePayment(expenseId: String, paymentId: String) { record("deletePayment", expenseId, paymentId); onDeletePayment(expenseId, paymentId) }
    override fun saveEventExpense(groupId: String, expense: EventExpense) { record("saveEventExpense", groupId, expense); onSaveEventExpense(groupId, expense) }
    override fun deleteEventExpense(groupId: String, expense: EventExpense) { record("deleteEventExpense", groupId, expense); onDeleteEventExpense(groupId, expense) }
    override fun getExpenseById(expenseId: String): Expense { record("getExpenseById", expenseId); return onGetExpenseById(expenseId) }
    override fun getGroupById(id: String): Group { record("getGroupById", id); return onGetGroupById(id) }
    override fun getEventExpenseById(groupId: String, expenseId: String?, eventId: String): EventExpense { record("getEventExpenseById", groupId, expenseId, eventId); return onGetEventExpenseById(groupId, expenseId, eventId) }
    override fun getEventPaymentById(groupId: String, paymentId: String?, eventId: String): EventPayment { record("getEventPaymentById", groupId, paymentId, eventId); return onGetEventPaymentById(groupId, paymentId, eventId) }
    override fun getUUID(): String { record("getUUID"); return onGetUUID() }
    override fun saveEventPayment(groupId: String, savedPayment: EventPayment) { record("saveEventPayment", groupId, savedPayment); onSaveEventPayment(groupId, savedPayment) }
    override fun deleteEventPayment(groupId: String, payment: EventPayment) { record("deleteEventPayment", groupId, payment); onDeleteEventPayment(groupId, payment) }
    override fun updateProfileImage(imagePath: String) { record("updateProfileImage", imagePath); onUpdateProfileImage(imagePath) }
    override fun updateUserName(newName: String) { record("updateUserName", newName); onUpdateUserName(newName) }
    override fun sendFriendRequest(friend: UserInfo) { record("sendFriendRequest", friend); onSendFriendRequest(friend) }
    override fun acceptFriendRequest(friend: UserInfo) { record("acceptFriendRequest", friend); onAcceptFriendRequest(friend) }
    override fun rejectFriendRequest(friend: UserInfo) { record("rejectFriendRequest", friend); onRejectFriendRequest(friend) }
    override fun cancelFriendRequest(friend: UserInfo) { record("cancelFriendRequest", friend); onCancelFriendRequest(friend) }
    override fun removeFriend(friend: UserInfo) { record("removeFriend", friend); onRemoveFriend(friend) }
    override fun getGroupMembers(groupId: String): List<UserInfo> { record("getGroupMembers", groupId); return onGetGroupMembers(groupId) }
    override fun getGroupMembersWithGuests(groupId: String): List<UserInfo> { record("getGroupMembersWithGuests", groupId); return onGetGroupMembersWithGuests(groupId) }
    override fun setGroupMembers(group: Group, userInfo: List<UserInfo>) { record("setGroupMembers", group, userInfo); onSetGroupMembers(group, userInfo) }
    override fun getEventById(groupId: String, eventId: String?): Event { record("getEventById", groupId, eventId); return onGetEventById(groupId, eventId) }
    override fun saveEvent(groupId: String, event: Event) { record("saveEvent", groupId, event); onSaveEvent(groupId, event) }
    override fun deleteEvent(groupId: String, eventId: String) { record("deleteEvent", groupId, eventId); onDeleteEvent(groupId, eventId) }
    override fun settleEvent(groupId: String, eventId: String) { record("settleEvent", groupId, eventId); onSettleEvent(groupId, eventId) }
    override fun reopenEvent(groupId: String, eventId: String) { record("reopenEvent", groupId, eventId); onReopenEvent(groupId, eventId) }
    override fun recalculateEventDebts(groupId: String, eventId: String) { record("recalculateEventDebts", groupId, eventId); onRecalculateEventDebts(groupId, eventId) }
    override fun updateGroupInState(groupId: String, updatedGroup: Group) { record("updateGroupInState", groupId, updatedGroup); onUpdateGroupInState(groupId, updatedGroup) }
    override fun updateEventInState(groupId: String, eventId: String, updatedEvent: Event) { record("updateEventInState", groupId, eventId, updatedEvent); onUpdateEventInState(groupId, eventId, updatedEvent) }
}