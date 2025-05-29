package com.ragl.divide.data.repositories

import com.ragl.divide.data.models.ExpenseType
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupEvent
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import com.ragl.divide.data.services.GroupEventService
import com.ragl.divide.data.services.GroupExpenseService
import dev.gitlive.firebase.database.FirebaseDatabase
import dev.gitlive.firebase.storage.File
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

interface GroupRepository {
    suspend fun getGroups(groupIds: Map<String, String>): Map<String, Group>
    suspend fun getGroup(id: String): Group
    suspend fun saveGroup(group: Group, photo: File?): Group
    suspend fun uploadPhoto(photo: File, id: String): String
    suspend fun getPhoto(id: String): String
    suspend fun addUser(groupId: String, userId: String)
    suspend fun getUsers(userIds: Collection<String>): List<User>
    suspend fun leaveGroup(groupId: String)
    suspend fun deleteGroup(groupId: String, image: String)
    suspend fun saveExpense(groupId: String, expense: GroupExpense): GroupExpense
    suspend fun deleteExpense(groupId: String, expense: GroupExpense)
    suspend fun savePayment(groupId: String, payment: Payment): Payment
    suspend fun deletePayment(groupId: String, payment: Payment)
    suspend fun saveEvent(groupId: String, event: GroupEvent): GroupEvent
    suspend fun deleteEvent(groupId: String, eventId: String)
    suspend fun getEvent(groupId: String, eventId: String): GroupEvent
    suspend fun getEvents(groupId: String): Map<String, GroupEvent>
    suspend fun saveRecurringExpense(groupId: String, expense: GroupExpense): GroupExpense
    suspend fun updateRecurringExpense(groupId: String, expense: GroupExpense): GroupExpense
    suspend fun deleteRecurringExpense(groupId: String, expenseId: String)
}

class GroupRepositoryImpl(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val userRepository: UserRepository,
    private val groupExpenseService: GroupExpenseService,
    private val groupEventService: GroupEventService
) : GroupRepository {
    init {
        //database.reference("groups")
    }

    override suspend fun getGroups(groupIds: Map<String, String>): Map<String, Group> {
        val groups = mutableMapOf<String, Group>()
        groupIds.values.mapNotNull {
            database.reference("groups/$it").valueEvents.firstOrNull()?.value<Group>()
        }.forEach { groups[it.id] = it }
        return groups
    }

    override suspend fun getGroup(id: String): Group {
        val group = database.reference("groups/$id").valueEvents.firstOrNull()?.value<Group>()
            ?: Group()
        return group.copy(image = if (group.image.isNotEmpty()) getPhoto(id) else "")
    }

    override suspend fun saveGroup(group: Group, photo: File?): Group {
        val id = group.id.ifEmpty { "id${Clock.System.now().toEpochMilliseconds()}" }
        val uid = userRepository.getFirebaseUser()!!.uid
        val savedGroup = group.copy(
            users = group.users + (uid to uid),
            image = if (photo != null) uploadPhoto(photo, id) else group.image,
            id = id
        )
        database.reference("groups/$id").setValue(savedGroup)
        coroutineScope {
            savedGroup.users.forEach {
                launch {
                    userRepository.saveGroup(id, it.key)
                }
            }
        }
        updateCurrentDebts(id)
        return savedGroup
    }

    override suspend fun uploadPhoto(photo: File, id: String): String {
        val photoRef = storage.reference("groupPhotos/$id.jpg")
        photoRef.putFile(photo)
        return photoRef.getDownloadUrl()
    }

    override suspend fun getPhoto(id: String): String {
        val storageRef = storage.reference("groupPhotos/$id.jpg")
        return storageRef.getDownloadUrl()
    }

    override suspend fun addUser(groupId: String, userId: String) {
        val groupRef = database.reference("groups/$groupId/users")
        groupRef.child(userId).setValue(userId)
    }

    override suspend fun getUsers(userIds: Collection<String>): List<User> = userIds.map {
        userRepository.getUser(it)
    }

    override suspend fun leaveGroup(groupId: String) {
        val user = userRepository.getFirebaseUser() ?: return
        val groupRef = database.reference("groups/$groupId/users")
        groupRef.child(user.uid).removeValue()
        val userRef = database.reference("users/${user.uid}/groups")
        userRef.child(groupId).removeValue()
    }

    override suspend fun deleteGroup(groupId: String, image: String) {
        if (image.isNotEmpty()) {
            storage.reference("groupPhotos/$image.jpg").delete()
        }

        val groupRef = database.reference("groups/$groupId")
        val userIds = groupRef.child("users").valueEvents.firstOrNull()?.children?.mapNotNull {
            it.key
        }
        coroutineScope {
            userIds?.forEach { userId ->
                launch {
                    userRepository.leaveGroup(groupId, userId)
                }
            }
        }

        groupRef.removeValue()
    }

    override suspend fun saveExpense(groupId: String, expense: GroupExpense): GroupExpense {
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

        updateCurrentDebts(groupId, expense.eventId)
        return newExpense
    }

    override suspend fun deleteExpense(groupId: String, expense: GroupExpense) {
        val groupRef = database.reference("groups/$groupId")
        if (expense.eventId.isNotEmpty())
            groupRef.child("events/${expense.eventId}/expenses").child(expense.id).removeValue()
        else
            groupRef.child("expenses").child(expense.id).removeValue()

        updateCurrentDebts(groupId, expense.eventId)
    }

    override suspend fun savePayment(groupId: String, payment: Payment): Payment {
        val newPayment = payment.copy(id = payment.id.ifEmpty {
            "id${Clock.System.now().toEpochMilliseconds()}"
        })
        val groupRef = database.reference("groups/$groupId")
        if (payment.eventId.isNotEmpty())
            groupRef.child("events/${payment.eventId}/payments").child(newPayment.id)
                .setValue(newPayment)
        else
            groupRef.child("payments").child(newPayment.id).setValue(newPayment)

        updateCurrentDebts(groupId, payment.eventId)
        return newPayment
    }

    override suspend fun deletePayment(groupId: String, payment: Payment) {
        val groupRef = database.reference("groups/$groupId")
        if (payment.eventId.isNotEmpty())
            groupRef.child("events/${payment.eventId}/payments").child(payment.id).removeValue()
        else
            groupRef.child("payments").child(payment.id).removeValue()

        updateCurrentDebts(groupId, payment.eventId)
    }

    override suspend fun saveEvent(groupId: String, event: GroupEvent): GroupEvent {
        val newEvent = event.copy(
            id = event.id.ifEmpty { "event${Clock.System.now().toEpochMilliseconds()}" },
        )
        val groupRef = database.reference("groups/$groupId")
        groupRef.child("events").child(newEvent.id).setValue(newEvent)
        return newEvent
    }

    override suspend fun getEvent(groupId: String, eventId: String): GroupEvent {
        val groupRef = database.reference("groups/$groupId")
        return groupRef.child("events").child(eventId).valueEvents.firstOrNull()
            ?.value<GroupEvent>() ?: GroupEvent()
    }

    override suspend fun getEvents(groupId: String): Map<String, GroupEvent> {
        val events = mutableMapOf<String, GroupEvent>()
        val groupRef = database.reference("groups/$groupId")
        val snapshot = groupRef.child("events").valueEvents.firstOrNull()
        snapshot?.children?.forEach {
            it.value<GroupEvent>().let { event -> events[event.id] = event }
        }
        return events
    }

    override suspend fun deleteEvent(groupId: String, eventId: String) {
        val groupRef = database.reference("groups/$groupId")
        groupRef.child("events").child(eventId).removeValue()
    }

    override suspend fun saveRecurringExpense(
        groupId: String,
        expense: GroupExpense
    ): GroupExpense {
        val newExpense = expense.copy(
            id = expense.id.ifEmpty { "id${Clock.System.now().toEpochMilliseconds()}" },
            expenseType = ExpenseType.RECURRING
        )
        val groupRef = database.reference("groups/$groupId")
        groupRef.child("recurringExpenses").child(newExpense.id).setValue(newExpense)

        // También actualizar las deudas generales del grupo
        updateCurrentDebts(groupId)

        return newExpense
    }

    override suspend fun updateRecurringExpense(
        groupId: String,
        expense: GroupExpense
    ): GroupExpense {
        val groupRef = database.reference("groups/$groupId")
        groupRef.child("recurringExpenses").child(expense.id).setValue(expense)

        // También actualizar las deudas generales del grupo
        updateCurrentDebts(groupId)

        return expense
    }

    override suspend fun deleteRecurringExpense(groupId: String, expenseId: String) {
        val groupRef = database.reference("groups/$groupId")
        groupRef.child("recurringExpenses").child(expenseId).removeValue()

        // También actualizar las deudas generales del grupo
        updateCurrentDebts(groupId)
    }

    private suspend fun updateCurrentDebts(groupId: String, eventId: String? = null) {
        val groupRef = database.reference("groups/$groupId")

        val expenses =
            if (!eventId.isNullOrEmpty())
                groupRef.child("events/$eventId/expenses").valueEvents.firstOrNull()?.children?.map {
                    it.value<GroupExpense>()
                } ?: emptyList()
            else
                groupRef.child("expenses").valueEvents.firstOrNull()?.children?.map {
                    it.value<GroupExpense>()
                } ?: emptyList()

        val payments =
            if (!eventId.isNullOrEmpty())
                groupRef.child("events/$eventId/payments").valueEvents.firstOrNull()?.children?.map {
                    it.value<Payment>()
                } ?: emptyList()
            else
                groupRef.child("payments").valueEvents.firstOrNull()?.children?.map {
                    it.value<Payment>()
                } ?: emptyList()

        val simplifyDebts =
            groupRef.child("simplifyDebts").valueEvents.firstOrNull()?.value<Boolean>() ?: false

        // Listas para almacenar IDs de gastos y pagos a liquidar
        val expensesToSettle = mutableListOf<String>()
        val paymentsToSettle = mutableListOf<String>()

        // Calcular deudas actuales, potencialmente identificando gastos/pagos a liquidar
        val currentDebts = groupExpenseService.calculateDebts(
            expenses,
            payments,
            simplifyDebts,
            expensesToSettle,
            paymentsToSettle
        )

        // Crear un mapa de actualizaciones para realizar en una única transacción
        val updates = mutableMapOf<String, Any?>()

        // Agregar actualización de deudas actuales
        updates["currentDebts"] = currentDebts

        // Si hay gastos o pagos para liquidar (cuando calculateDebts detectó que no hay deudas)
        if (expensesToSettle.isNotEmpty() || paymentsToSettle.isNotEmpty()) {
            // Agregar actualizaciones para marcar gastos como liquidados
            for (expenseId in expensesToSettle) {
                updates["expenses/$expenseId/settled"] = true
            }

            // Agregar actualizaciones para marcar pagos como liquidados
            for (paymentId in paymentsToSettle) {
                updates["payments/$paymentId/settled"] = true
            }
        }

        // Ejecutar todas las actualizaciones en una única transacción
        if (!eventId.isNullOrEmpty())
            groupRef.child("events/$eventId").updateChildren(updates)
        else
            groupRef.updateChildren(updates)
    }
}