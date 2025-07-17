package com.ragl.divide.data.stateHolders

import com.ragl.divide.data.models.Event
import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.data.models.EventPayment
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.repositories.FriendsRepository
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.AnalyticsService
import com.ragl.divide.domain.services.AppStateService
import com.ragl.divide.domain.services.GroupExpenseService
import com.ragl.divide.domain.stateHolders.UserState
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.utils.logMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class UserStateHolderImpl(
    private val userRepository: UserRepository,
    private val friendsRepository: FriendsRepository,
    private val groupRepository: GroupRepository,
    private val analyticsService: AnalyticsService,
    private val groupExpenseService: GroupExpenseService,
    private val appStateService: AppStateService
) : UserStateHolder {
    private val _state = MutableStateFlow(UserState())
    override val userState: StateFlow<UserState> = _state

    override fun updateUserState(userState: UserState) {
        _state.update { userState }
    }

    override fun updateUser(user: User) {
        _state.update { it.copy(user = user) }
    }

    override fun updateGroups(groups: Map<String, Group>) {
        _state.update { it.copy(groups = groups) }
    }

    override fun updateGroupMembers(groupMembers: Map<String, List<UserInfo>>) {
        _state.update { it.copy(groupMembers = groupMembers) }
    }

    override fun updateFriends(friends: Map<String, UserInfo>) {
        _state.update { it.copy(friends = friends) }
    }

    override fun updateFriendRequestsReceived(friendRequestsReceived: Map<String, UserInfo>) {
        _state.update { it.copy(friendRequestsReceived = friendRequestsReceived) }
    }

    override fun updateFriendRequestsSent(friendRequestsSent: Map<String, UserInfo>) {
        _state.update { it.copy(friendRequestsSent = friendRequestsSent) }
    }

    override fun removeExpense(expenseId: String) {
        _state.update {
            it.copy(user = it.user.copy(expenses = it.user.expenses - expenseId))
        }
    }

    override fun deleteGroup(groupId: String) {
        _state.update {
            it.copy(groups = it.groups - groupId)
        }
    }

    override fun saveGroup(group: Group) {
        _state.update {
            it.copy(groups = it.groups + (group.id to group))
        }
        group.events.values.map {
            if (!it.settled) {
                recalculateEventDebts(group.id, it.id)
            }
        }
    }

    override fun saveExpense(expense: Expense) {
        _state.update {
            it.copy(user = it.user.copy(expenses = it.user.expenses + (expense.id to expense)))
        }
    }

    override fun savePayment(expenseId: String, payment: Payment) {
        _state.update {
            it.copy(user = it.user.copy(expenses = it.user.expenses.mapValues { expense ->
                if (expense.key == expenseId) {
                    expense.value.copy(
                        payments = expense.value.payments + (payment.id to payment),
                        amountPaid = expense.value.amountPaid + payment.amount,
                        paid = expense.value.amountPaid + payment.amount == expense.value.amount
                    )
                } else {
                    expense.value
                }
            }))
        }
    }

    override fun deletePayment(expenseId: String, paymentId: String) {
        _state.update {
            it.copy(user = it.user.copy(expenses = it.user.expenses.mapValues { expense ->
                if (expense.key == expenseId) {
                    expense.value.copy(
                        payments = expense.value.payments - paymentId,
                        amountPaid = expense.value.amountPaid - expense.value.payments[paymentId]!!.amount,
                        paid = false
                    )
                } else {
                    expense.value
                }
            }))
        }
    }

    override fun saveEventExpense(groupId: String, expense: EventExpense) {
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        group.value.copy(events = group.value.events.mapValues { event ->
                            if (event.key == expense.eventId) {
                                event.value.copy(expenses = event.value.expenses + (expense.id to expense))
                            } else {
                                event.value
                            }
                        })
                    } else {
                        group.value
                    }
                }
            )
        }
        // Solo recalcular si es un gasto de evento
        if (expense.eventId.isNotEmpty()) {
            recalculateEventDebts(groupId, expense.eventId)
        }
    }

    override fun deleteEventExpense(groupId: String, expense: EventExpense) {
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        group.value.copy(events = group.value.events.mapValues { event ->
                            if (event.key == expense.eventId) {
                                event.value.copy(expenses = event.value.expenses - expense.id)
                            } else {
                                event.value
                            }
                        })
                    } else {
                        group.value
                    }
                }
            )
        }
        // Solo recalcular si es un gasto de evento
        if (expense.eventId.isNotEmpty()) {
            recalculateEventDebts(groupId, expense.eventId)
        }
    }

    override fun getExpenseById(expenseId: String): Expense {
        return _state.value.user.expenses[expenseId] ?: Expense()
    }

    override fun getGroupById(id: String): Group {
        return _state.value.groups[id] ?: Group()
    }

    override fun getEventExpenseById(
        groupId: String,
        expenseId: String?,
        eventId: String
    ): EventExpense {
        return _state.value.groups[groupId]?.events?.get(eventId)?.expenses?.get(expenseId)
            ?: EventExpense()
    }

    override fun getEventPaymentById(
        groupId: String,
        paymentId: String?,
        eventId: String
    ): EventPayment {
        return _state.value.groups[groupId]?.events?.get(eventId)?.payments?.get(paymentId)
            ?: EventPayment()
    }

    override fun getUUID(): String {
        return _state.value.user.uuid
    }

    override fun saveEventPayment(groupId: String, savedPayment: EventPayment) {
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        group.value.copy(events = group.value.events.mapValues { event ->
                            if (event.key == savedPayment.eventId) {
                                event.value.copy(payments = event.value.payments + (savedPayment.id to savedPayment))
                            } else {
                                event.value
                            }
                        })
                    } else {
                        group.value
                    }
                }
            )
        }
        // Solo recalcular si es un pago de evento
        if (savedPayment.eventId.isNotEmpty()) {
            recalculateEventDebts(groupId, savedPayment.eventId)
        }
    }

    override fun deleteEventPayment(groupId: String, payment: EventPayment) {
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        group.value.copy(events = group.value.events.mapValues { event ->
                            if (event.key == payment.eventId) {
                                event.value.copy(payments = event.value.payments - payment.id)
                            } else {
                                event.value
                            }
                        })
                    } else {
                        group.value
                    }
                }
            )
        }
        // Solo recalcular si es un pago de evento
        if (payment.eventId.isNotEmpty()) {
            recalculateEventDebts(groupId, payment.eventId)
        }
    }


    override fun updateProfileImage(imagePath: String) {
        _state.update {
            it.copy(user = it.user.copy(photoUrl = imagePath))
        }
    }

    override fun updateUserName(newName: String) {
        _state.update {
            it.copy(user = it.user.copy(name = newName))
        }
    }

    override fun sendFriendRequest(friend: UserInfo) {
        _state.update {
            it.copy(friendRequestsSent = it.friendRequestsSent + (friend.uuid to friend))
        }
    }

    override fun acceptFriendRequest(friend: UserInfo) {
        _state.update {
            it.copy(
                friendRequestsReceived = it.friendRequestsReceived - friend.uuid,
                friends = it.friends + (friend.uuid to friend)
            )
        }
    }

    override fun rejectFriendRequest(friend: UserInfo) {
        _state.update {
            it.copy(friendRequestsReceived = it.friendRequestsReceived - friend.uuid)
        }
    }

    override fun cancelFriendRequest(friend: UserInfo) {
        _state.update {
            it.copy(friendRequestsSent = it.friendRequestsSent - friend.uuid)
        }
    }

    override fun removeFriend(friend: UserInfo) {
        _state.update {
            it.copy(friends = it.friends - friend.uuid)
        }
    }

    override fun getGroupMembers(groupId: String): List<UserInfo> {
        return _state.value.groupMembers[groupId] ?: emptyList()
    }

    override fun getGroupMembersWithGuests(groupId: String): List<UserInfo> {
        val group = _state.value.groups[groupId] ?: return emptyList()
        val members = _state.value.groupMembers[groupId] ?: emptyList()

        // Crear UserInfo para los invitados
        val guests = group.guests.map { (guestId, guestName) ->
            UserInfo(
                uuid = guestId,
                name = guestName,
                photoUrl = "" // Los invitados no tienen foto
            )
        }

        return members + guests
    }

    override fun setGroupMembers(group: Group, userInfo: List<UserInfo>) {
        _state.update { state ->
            state.copy(
                groupMembers = state.groupMembers + (group.id to userInfo)
            )
        }
    }

    override fun getEventById(groupId: String, eventId: String?): Event {
        return _state.value.groups[groupId]?.events?.get(eventId) ?: Event()
    }

    override fun saveEvent(groupId: String, event: Event) {
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        group.value.copy(events = group.value.events + (event.id to event))
                    } else {
                        group.value
                    }
                }
            )
        }
        // Recalcular las deudas del evento recién guardado
        recalculateEventDebts(groupId, event.id)
    }

    override fun deleteEvent(groupId: String, eventId: String) {
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        group.value.copy(events = group.value.events - eventId)
                    } else {
                        group.value
                    }
                }
            )
        }
    }

    override fun settleEvent(groupId: String, eventId: String) {
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        group.value.copy(
                            events = group.value.events.mapValues { event ->
                                if (event.key == eventId) {
                                    event.value.copy(
                                        settled = true
                                    )
                                } else {
                                    event.value
                                }
                            }
                        )
                    } else {
                        group.value
                    }
                }
            )
        }
    }

    override fun reopenEvent(groupId: String, eventId: String) {
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        group.value.copy(
                            events = group.value.events.mapValues { event ->
                                if (event.key == eventId) {
                                    event.value.copy(
                                        settled = false
                                    )
                                } else {
                                    event.value
                                }
                            }
                        )
                    } else {
                        group.value
                    }
                }
            )
        }
        // Recalcular las deudas del evento reabierto
        recalculateEventDebts(groupId, eventId)
    }

    override fun recalculateEventDebts(groupId: String, eventId: String) {
        val group = _state.value.groups[groupId] ?: return
        val simplifyDebts = group.simplifyDebts

        val expenses = group.events[eventId]?.expenses?.values?.toList() ?: emptyList()
        val payments = group.events[eventId]?.payments?.values?.toList() ?: emptyList()

        val currentDebts = groupExpenseService.calculateDebts(
            expenses,
            payments,
            simplifyDebts,
        )

        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        group.value.copy(
                            events = group.value.events.mapValues { event ->
                                if (event.key == eventId)
                                    event.value.copy(
                                        currentDebts = currentDebts,
                                    )
                                else
                                    event.value
                            }
                        )
                    } else {
                        group.value
                    }
                }
            )
        }
    }

    override fun updateGroupInState(groupId: String, updatedGroup: Group) {
        _state.update { currentState ->
            currentState.copy(
                groups = currentState.groups + (groupId to updatedGroup)
            )
        }
    }

    override fun updateEventInState(groupId: String, eventId: String, updatedEvent: Event) {
        _state.update { currentState ->
            val currentGroup = currentState.groups[groupId]
            if (currentGroup != null) {
                val updatedGroup = currentGroup.copy(
                    events = currentGroup.events + (eventId to updatedEvent)
                )
                currentState.copy(
                    groups = currentState.groups + (groupId to updatedGroup)
                )
            } else {
                currentState
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun refreshUser() {
        CoroutineScope(Dispatchers.IO).launch {
            appStateService.showLoading()
            val startTime = Clock.System.now().toEpochMilliseconds()
            try {
                val user = userRepository.getUser(userRepository.getFirebaseUser()!!.uid)
                analyticsService.setUserProperties(user.uuid, user.name)
                val userInfo = UserInfo(user.uuid, user.name, user.photoUrl)
                val friends = friendsRepository.getFriends(user.friends.keys.toList())
                val groups = groupRepository.getGroups(user.groups)
                val groupMembers = groups.mapValues { (_, group) ->
                    friendsRepository.getGroupMembers(
                        group.users.values.toList(),
                        friends + (user.uuid to userInfo)
                    )
                }
                val friendRequestsReceived =
                    friendsRepository.getFriendRequestsReceived(user.friendRequestsReceived)
                val friendRequestsSent =
                    friendsRepository.getFriendRequestsSent(user.friendRequestsSent)
                updateUserState(
                    UserState(
                        user = user,
                        groups = groups,
                        groupMembers = groupMembers,
                        friends = friends,
                        friendRequestsReceived = friendRequestsReceived,
                        friendRequestsSent = friendRequestsSent
                    )
                )
            } catch (e: Exception) {
                analyticsService.logError(e, "Error al obtener datos del usuario")
            } finally {
                appStateService.hideLoading()
            }
            val timeTaken = Clock.System.now().toEpochMilliseconds() - startTime
            logMessage("UserStateHolder", "refreshUser completed in $timeTaken ms")
        }
    }
}