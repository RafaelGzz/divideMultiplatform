package com.ragl.divide.domain.repositories

import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.Event
import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.data.models.EventPayment
import com.ragl.divide.data.models.User
import dev.gitlive.firebase.storage.File

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
    suspend fun saveGroupExpense(groupId: String, expense: EventExpense): EventExpense
    suspend fun deleteGroupExpense(groupId: String, expense: EventExpense)
    suspend fun saveGroupPayment(groupId: String, payment: EventPayment): EventPayment
    suspend fun deleteGroupPayment(groupId: String, payment: EventPayment)
    suspend fun saveEvent(groupId: String, event: Event): Event
    suspend fun deleteEvent(groupId: String, eventId: String)
    suspend fun getEvent(groupId: String, eventId: String): Event
    suspend fun getEvents(groupId: String): Map<String, Event>
    suspend fun settleEvent(groupId: String, eventId: String)
    suspend fun reopenEvent(groupId: String, eventId: String)
    suspend fun saveRecurringExpense(groupId: String, expense: EventExpense): EventExpense
    suspend fun updateRecurringExpense(groupId: String, expense: EventExpense): EventExpense
    suspend fun deleteRecurringExpense(groupId: String, expenseId: String)
}