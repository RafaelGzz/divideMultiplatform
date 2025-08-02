package com.ragl.divide.data.repositories

import com.ragl.divide.data.models.Event
import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.data.models.EventPayment
import com.ragl.divide.data.models.ExpenseType
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.User
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.GroupExpenseService
import com.ragl.divide.presentation.utils.logMessage
import dev.gitlive.firebase.database.DatabaseReference
import dev.gitlive.firebase.database.FirebaseDatabase
import dev.gitlive.firebase.storage.File
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class GroupRepositoryImpl(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val userRepository: UserRepository,
    private val groupExpenseService: GroupExpenseService
) : GroupRepository {
    init {
        //database.reference("groups")
    }

    override suspend fun getGroups(groupIds: Map<String, String>): Map<String, Group> =
        coroutineScope {
            val startTime = Clock.System.now().toEpochMilliseconds()
            val deferredResults = groupIds.values.map { groupId ->
                async {
                    database.reference("groups/$groupId").valueEvents.firstOrNull()?.value<Group>()
                }
            }

            val results = deferredResults.awaitAll()
            val groups = mutableMapOf<String, Group>()

            results.filterNotNull().forEach { group ->
                groups[group.id] = group
            }

            val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
            logMessage(
                "GroupRepositoryImpl",
                "getGroups: ${groups.size} - executed in ${executionTime}ms"
            )
            groups
        }

    override suspend fun getGroup(id: String): Group {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val group = database.reference("groups/$id").valueEvents.firstOrNull()?.value<Group>()
            ?: Group()
        //val groupWithImage = group.copy(image = if (group.image.isNotEmpty()) getPhoto(id) else "")
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage(
            "GroupRepositoryImpl",
            "getGroup: ${group.id} - executed in ${executionTime}ms"
        )
        return group
    }

    override suspend fun saveGroup(group: Group, photo: File?): Group {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val id = group.id.ifEmpty { "id${Clock.System.now().toEpochMilliseconds()}" }
        val uid = userRepository.getCurrentUser()!!.uid
        val savedGroup = group.copy(
            users = group.users + (uid to uid),
            image = if (photo != null) uploadPhoto(photo, id) else group.image,
            id = id
        )
        database.reference("groups/$id").setValue(savedGroup)
        coroutineScope {
            savedGroup.users.forEach {
                async {
                    userRepository.addGroupToUser(id, it.key)
                }
            }
        }

        group.events.values.map {
            if (!it.settled)
                updateCurrentDebts(group.id, it.id)
        }
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage(
            "GroupRepositoryImpl",
            "saveGroup: ${savedGroup.id} - executed in ${executionTime}ms"
        )

        return savedGroup
    }

    override suspend fun uploadPhoto(photo: File, id: String): String {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val photoRef = storage.reference("groupPhotos/$id.jpg")
        photoRef.putFile(photo)
        val downloadUrl = photoRef.getDownloadUrl()
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage(
            "GroupRepositoryImpl",
            "uploadPhoto: $downloadUrl - executed in ${executionTime}ms"
        )
        return downloadUrl
    }

    override suspend fun getPhoto(id: String): String {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val storageRef = storage.reference("groupPhotos/$id.jpg")
        val downloadUrl = storageRef.getDownloadUrl()
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("GroupRepositoryImpl", "getPhoto: $downloadUrl - executed in ${executionTime}ms")
        return downloadUrl
    }

    override suspend fun addUser(groupId: String, userId: String) {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val groupRef = database.reference("groups/$groupId/users")
        groupRef.child(userId).setValue(userId)
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("GroupRepositoryImpl", "addUser: $userId - executed in ${executionTime}ms")
    }

    override suspend fun getUsers(userIds: Collection<String>): List<User> = coroutineScope {
        val deferredResults = userIds.map { userId ->
            async {
                userRepository.getUser(userId)
            }
        }

        deferredResults.awaitAll()
    }

    override suspend fun leaveGroup(groupId: String) {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val user = userRepository.getCurrentUser() ?: return
        val groupRef = database.reference("groups/$groupId/users")
        groupRef.child(user.uid).removeValue()
        val userRef = database.reference("users/${user.uid}/groups")
        userRef.child(groupId).removeValue()
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("GroupRepositoryImpl", "leaveGroup: $groupId - executed in ${executionTime}ms")
    }

    override suspend fun deleteGroup(groupId: String, image: String) {
        val startTime = Clock.System.now().toEpochMilliseconds()
        coroutineScope {
            if (image.isNotEmpty()) {
                async {
                    val start = Clock.System.now().toEpochMilliseconds()
                    storage.reference("groupPhotos/$image.jpg").delete()
                    val execution = Clock.System.now().toEpochMilliseconds() - start
                    logMessage("GroupRepositoryImpl", "deletePhoto - executed in ${execution}ms")
                }
            }

            val groupRef = database.reference("groups/$groupId")
            val userIds = groupRef.child("users").valueEvents.firstOrNull()?.children?.mapNotNull {
                it.key
            }
            userIds?.forEach { userId ->
                async {
                    userRepository.removeGroupFromUser(groupId, userId)
                }
            }
            groupRef.removeValue()
        }

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("GroupRepositoryImpl", "deleteGroup: $groupId - executed in ${executionTime}ms")
    }

    override suspend fun saveEventExpense(groupId: String, expense: EventExpense): EventExpense {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val newExpense = expense.copy(
            id = expense.id.ifEmpty {
                "id${Clock.System.now().toEpochMilliseconds()}"
            }
        )
        val groupRef = database.reference("groups/$groupId")
        if (expense.eventId.isNotEmpty()) {
            groupRef.child("events/${expense.eventId}/expenses").child(newExpense.id)
                .setValue(newExpense)
        } else
            groupRef.child("expenses").child(newExpense.id).setValue(newExpense)

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage(
            "GroupRepositoryImpl",
            "saveGroupExpense: $newExpense - executed in ${executionTime}ms"
        )

        updateCurrentDebts(groupId, expense.eventId)
        return newExpense
    }

    override suspend fun deleteEventExpense(groupId: String, expense: EventExpense) {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val groupRef = database.reference("groups/$groupId")
        if (expense.eventId.isNotEmpty())
            groupRef.child("events/${expense.eventId}/expenses").child(expense.id).removeValue()
        else
            groupRef.child("expenses").child(expense.id).removeValue()

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage(
            "GroupRepositoryImpl",
            "deleteExpense: $expense - executed in ${executionTime}ms"
        )

        updateCurrentDebts(groupId, expense.eventId)
    }

    override suspend fun saveEventPayment(groupId: String, payment: EventPayment): EventPayment {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val newPayment = payment.copy(id = payment.id.ifEmpty {
            "id${Clock.System.now().toEpochMilliseconds()}"
        })
        val groupRef = database.reference("groups/$groupId")
        if (payment.eventId.isNotEmpty())
            groupRef.child("events/${payment.eventId}/payments").child(newPayment.id)
                .setValue(newPayment)
        else
            groupRef.child("payments").child(newPayment.id).setValue(newPayment)

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage(
            "GroupRepositoryImpl",
            "savePayment: $newPayment - executed in ${executionTime}ms"
        )

        updateCurrentDebts(groupId, payment.eventId)
        return newPayment
    }

    override suspend fun deleteEventPayment(groupId: String, payment: EventPayment) {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val groupRef = database.reference("groups/$groupId")
        if (payment.eventId.isNotEmpty())
            groupRef.child("events/${payment.eventId}/payments").child(payment.id).removeValue()
        else
            groupRef.child("payments").child(payment.id).removeValue()

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage(
            "GroupRepositoryImpl",
            "deletePayment: $payment - executed in ${executionTime}ms"
        )

        updateCurrentDebts(groupId, payment.eventId)
    }

    override suspend fun saveEvent(groupId: String, event: Event): Event {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val newEvent = event.copy(
            id = event.id.ifEmpty { "event${Clock.System.now().toEpochMilliseconds()}" },
        )
        val groupRef = database.reference("groups/$groupId")
        groupRef.child("events").child(newEvent.id).setValue(newEvent)

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("GroupRepositoryImpl", "saveEvent: $newEvent - executed in ${executionTime}ms")

        updateCurrentDebts(groupId, newEvent.id)

        return newEvent
    }

    override suspend fun getEvent(groupId: String, eventId: String): Event {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val groupRef = database.reference("groups/$groupId")
        val result = groupRef.child("events").child(eventId).valueEvents.firstOrNull()
            ?.value<Event>() ?: Event()
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("GroupRepositoryImpl", "getEvent: $eventId - executed in ${executionTime}ms")
        return result
    }

    override suspend fun getEvents(groupId: String): Map<String, Event> = coroutineScope {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val groupRef = database.reference("groups/$groupId")
        val snapshot = groupRef.child("events").valueEvents.firstOrNull()
        val children = snapshot?.children?.toList() ?: emptyList()

        val deferredResults = children.map { childSnapshot ->
            async {
                childSnapshot.value<Event>()
            }
        }

        val results = deferredResults.awaitAll()
        val events = mutableMapOf<String, Event>()

        results.forEach { event ->
            events[event.id] = event
        }

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage(
            "GroupRepositoryImpl",
            "getEvents: ${events.size} - executed in ${executionTime}ms"
        )
        events
    }

    override suspend fun deleteEvent(groupId: String, eventId: String) {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val groupRef = database.reference("groups/$groupId")
        groupRef.child("events").child(eventId).removeValue()

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("GroupRepositoryImpl", "deleteEvent: $eventId - executed in ${executionTime}ms")
    }

    override suspend fun settleEvent(groupId: String, eventId: String) {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val groupRef = database.reference("groups/$groupId")
        groupRef.child("events/$eventId/settled").setValue(true)

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("GroupRepositoryImpl", "settleEvent: $eventId - executed in ${executionTime}ms")
    }

    override suspend fun reopenEvent(groupId: String, eventId: String) {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val groupRef = database.reference("groups/$groupId")
        groupRef.child("events/$eventId/settled").setValue(false)

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("GroupRepositoryImpl", "reopenEvent: $eventId - executed in ${executionTime}ms")
        updateCurrentDebts(groupId, eventId)
    }

    override suspend fun saveRecurringExpense(
        groupId: String,
        expense: EventExpense
    ): EventExpense {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val newExpense = expense.copy(
            id = expense.id.ifEmpty { "id${Clock.System.now().toEpochMilliseconds()}" },
            expenseType = ExpenseType.RECURRING
        )
        val groupRef = database.reference("groups/$groupId")
        groupRef.child("recurringExpenses").child(newExpense.id).setValue(newExpense)

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage(
            "GroupRepositoryImpl",
            "saveRecurringExpense: $newExpense - executed in ${executionTime}ms"
        )

        return newExpense
    }

    override suspend fun updateRecurringExpense(
        groupId: String,
        expense: EventExpense
    ): EventExpense {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val groupRef = database.reference("groups/$groupId")
        groupRef.child("recurringExpenses").child(expense.id).setValue(expense)

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage(
            "GroupRepositoryImpl",
            "updateRecurringExpense: $expense - executed in ${executionTime}ms"
        )

        return expense
    }

    override suspend fun deleteRecurringExpense(groupId: String, expenseId: String) {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val groupRef = database.reference("groups/$groupId")
        groupRef.child("recurringExpenses").child(expenseId).removeValue()

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage(
            "GroupRepositoryImpl",
            "deleteRecurringExpense: $expenseId - executed in ${executionTime}ms"
        )
    }

    private suspend fun updateCurrentDebts(groupId: String, eventId: String? = null) {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val groupRef = database.reference("groups/$groupId")

        if (!eventId.isNullOrEmpty()) {
            updateEventDebts(groupRef, eventId)
        }
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage(
            "GroupRepositoryImpl",
            "updateCurrentDebts: $eventId - executed in ${executionTime}ms"
        )
    }

    private suspend fun updateEventDebts(groupRef: DatabaseReference, eventId: String) =
        coroutineScope {
            val expensesDeferred = async {
                groupRef.child("events/$eventId/expenses").valueEvents.firstOrNull()?.children?.map {
                    it.value<EventExpense>()
                } ?: emptyList()
            }

            val paymentsDeferred = async {
                groupRef.child("events/$eventId/payments").valueEvents.firstOrNull()?.children?.map {
                    it.value<EventPayment>()
                } ?: emptyList()
            }

            val simplifyDebtsDeferred = async {
                groupRef.child("simplifyDebts").valueEvents.firstOrNull()?.value<Boolean>() == true
            }

            val expenses = expensesDeferred.await()
            val payments = paymentsDeferred.await()
            val simplifyDebts = simplifyDebtsDeferred.await()

            val currentDebts = groupExpenseService.calculateDebts(
                expenses,
                payments,
                simplifyDebts
            )

            val updates = mutableMapOf<String, Any?>()
            updates["currentDebts"] = currentDebts

            groupRef.child("events/$eventId").updateChildren(updates)
        }
}