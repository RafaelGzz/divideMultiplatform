package com.ragl.divide.data.repositories

import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.GroupUser
import com.ragl.divide.data.models.Method
import com.ragl.divide.data.models.User
import com.ragl.divide.ui.utils.toTwoDecimals
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
    suspend fun getUsers(userIds: List<String>): List<User>
    suspend fun leaveGroup(groupId: String)
    suspend fun deleteGroup(groupId: String, image: String)
    suspend fun saveExpense(groupId: String, expense: GroupExpense): GroupExpense
    suspend fun updateExpense(groupId: String, oldExpense: GroupExpense, newExpense: GroupExpense): GroupExpense
    suspend fun deleteExpense(groupId: String, expense: GroupExpense)
}

class GroupRepositoryImpl(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val userRepository: UserRepository
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
            users = group.users + (uid to GroupUser(id = uid)),
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

    override suspend fun getUsers(userIds: List<String>): List<User> = userIds.map {
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
        val id = "id${Clock.System.now().toEpochMilliseconds()}"
        val newExpense = expense.copy(id = id)
        val groupRef = database.reference("groups/$groupId")
        groupRef.child("expenses").child(id).setValue(newExpense)

        coroutineScope {
            expense.paidBy.entries.forEach { (payerId, amountPaid) ->
                val userRef = groupRef.child("users").child(payerId)
                val groupUser = userRef.valueEvents.firstOrNull()?.value<GroupUser>()!!
                val newOwedMap = groupUser.owed.toMutableMap()
                expense.debtors.entries.forEach { (debtorId, debtorAmount) ->
                    val debt = calculateDebt(expense, debtorAmount)
                    newOwedMap[debtorId] = ((newOwedMap[debtorId] ?: 0.0) + debt).toTwoDecimals()
                }
                val payerOwed = expense.amount - calculateDebt(expense, amountPaid)
                userRef.setValue(
                    groupUser.copy(
                        owed = newOwedMap,
                        totalOwed = (groupUser.totalOwed + payerOwed).toTwoDecimals()
                    )
                )
            }
            expense.debtors.entries.forEach { (debtorId, amount) ->
                launch {
                    val userRef = groupRef.child("users").child(debtorId)
                    val groupUser = userRef.valueEvents.firstOrNull()?.value<GroupUser>()!!
                    val newDebtsMap = groupUser.debts.toMutableMap()
                    val totalDebt = calculateDebt(expense, amount)
                    expense.paidBy.keys.forEach { payerId ->
                        newDebtsMap[payerId] = ((newDebtsMap[payerId] ?: 0.0) + totalDebt).toTwoDecimals()
                    }
                    userRef.setValue(
                        groupUser.copy(
                            debts = newDebtsMap,
                            totalDebt = (groupUser.totalDebt + totalDebt).toTwoDecimals()
                        )
                    )
                }
            }
        }

        return newExpense
    }

    override suspend fun updateExpense(groupId: String, oldExpense: GroupExpense, newExpense: GroupExpense): GroupExpense {
        val groupRef = database.reference("groups/$groupId")
        groupRef.child("expenses").child(newExpense.id).setValue(newExpense)

        coroutineScope {
            newExpense.paidBy.entries.forEach { (payerId, amountPaid) ->
                val userRef = groupRef.child("users").child(payerId)
                val groupUser = userRef.valueEvents.firstOrNull()?.value<GroupUser>()!!
                val newOwedMap = groupUser.owed.toMutableMap()
                oldExpense.debtors.entries.forEach { (debtorId, oldDebtorAmount) ->
                    val oldDebt = calculateDebt(oldExpense, oldDebtorAmount)
                    newOwedMap[debtorId] = ((newOwedMap[debtorId] ?: 0.0) - oldDebt).toTwoDecimals()
                }
                newExpense.debtors.entries.forEach { (debtorId, newDebtorAmount) ->
                    val newDebt = calculateDebt(newExpense, newDebtorAmount)
                    newOwedMap[debtorId] = ((newOwedMap[debtorId] ?: 0.0) + newDebt).toTwoDecimals()
                }
                val newPayerOwed = newExpense.amount - calculateDebt(newExpense, amountPaid)
                val oldPayerOwed = oldExpense.amount - calculateDebt(oldExpense, oldExpense.paidBy[payerId]!!)
                userRef.setValue(
                    groupUser.copy(
                        owed = newOwedMap,
                        totalOwed = (groupUser.totalOwed - oldPayerOwed + newPayerOwed).toTwoDecimals()
                    )
                )
            }

            newExpense.debtors.entries.forEach { (debtorId, amount) ->
                launch {
                    val userRef = groupRef.child("users").child(debtorId)
                    val groupUser = userRef.valueEvents.firstOrNull()?.value<GroupUser>()!!
                    val newDebtsMap = groupUser.debts.toMutableMap()
                    oldExpense.paidBy.entries.forEach { (payerId, payerAmount) ->
                        val oldDebt = calculateDebt(oldExpense, payerAmount)
                        newDebtsMap[payerId] = ((newDebtsMap[payerId] ?: 0.0) - oldDebt).toTwoDecimals()
                    }
                    newExpense.paidBy.entries.forEach { (payerId, payerAmount) ->
                        val newDebt = calculateDebt(newExpense, payerAmount)
                        newDebtsMap[payerId] = ((newDebtsMap[payerId] ?: 0.0) + newDebt).toTwoDecimals()
                    }
                    userRef.setValue(
                        groupUser.copy(
                            debts = newDebtsMap,
                            totalDebt = (groupUser.totalDebt - (oldExpense.debtors[debtorId] ?: 0.0) + amount).toTwoDecimals()
                        )
                    )
                }
            }
        }
        return newExpense
    }


    override suspend fun deleteExpense(groupId: String, expense: GroupExpense) {
        val groupRef = database.reference("groups/$groupId")
        groupRef.child("expenses").child(expense.id).removeValue()
        coroutineScope {
            expense.paidBy.entries.forEach { (userId, amountPaid) ->
                val userRef = groupRef.child("users").child(userId)
                val groupUser = userRef.valueEvents.firstOrNull()?.value<GroupUser>()!!
                val newOwedMap = groupUser.owed.toMutableMap()
                expense.debtors.entries.forEach { (debtorId, debtorAmount) ->
                    val debt = calculateDebt(expense, debtorAmount)
                    newOwedMap[debtorId] =
                        if (newOwedMap[debtorId] == null) 0.0 else (newOwedMap[debtorId]!! - debt).toTwoDecimals()
                }
                val payerOwed = expense.amount - calculateDebt(expense, amountPaid)
                userRef.setValue(
                    groupUser.copy(
                        totalOwed = (groupUser.totalOwed - payerOwed).toTwoDecimals(),
                        owed = newOwedMap
                    )
                )
            }
            expense.debtors.entries.forEach { (userId, amount) ->
                launch {
                    val userRef = groupRef.child("users").child(userId)
                    val groupUser = userRef.valueEvents.firstOrNull()?.value<GroupUser>()!!
                    val newDebtsMap = groupUser.debts.toMutableMap()
                    val totalDebt = calculateDebt(expense, amount)
                    expense.paidBy.keys.forEach { payerId ->
                        newDebtsMap[payerId] =
                            if (newDebtsMap[payerId] == null) 0.0 else (newDebtsMap[payerId]!! - totalDebt).toTwoDecimals()
                    }
                    userRef.setValue(
                        groupUser.copy(
                            totalDebt = (groupUser.totalDebt - totalDebt).toTwoDecimals(),
                            debts = newDebtsMap
                        )
                    )
                }
            }
        }
    }

    private fun calculateDebt(
        expense: GroupExpense,
        amount: Double
    ) = when (expense.splitMethod) {
        Method.EQUALLY, Method.CUSTOM -> amount
        Method.PERCENTAGES -> (amount * expense.amount) / 100
    }
}