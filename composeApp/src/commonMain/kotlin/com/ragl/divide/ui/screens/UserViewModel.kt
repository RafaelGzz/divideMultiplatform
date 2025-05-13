package com.ragl.divide.ui.screens

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.GroupUser
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.SplitMethod
import com.ragl.divide.data.models.User
import com.ragl.divide.data.repositories.FriendsRepository
import com.ragl.divide.data.repositories.GroupRepository
import com.ragl.divide.data.repositories.PreferencesRepository
import com.ragl.divide.data.repositories.UserRepository
import com.ragl.divide.ui.utils.logMessage
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppState(
    val isLoading: Boolean = false,
    val groups: Map<String, Group> = emptyMap(),
    val friends: Map<String, User> = emptyMap(),
    val selectedGroupMembers: List<User> = emptyList(),
    val user: User = User()
)

class UserViewModel(
    private var userRepository: UserRepository,
    private var friendsRepository: FriendsRepository,
    private val preferencesRepository: PreferencesRepository,
    private val groupRepository: GroupRepository
) : ScreenModel {

    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()

    private val _successState = MutableStateFlow<String?>(null)
    val successState = _successState.asStateFlow()

    var isDarkMode = MutableStateFlow<String?>(null)

    var startAtLogin = mutableStateOf(true)
        private set

    init {
        screenModelScope.launch {
            preferencesRepository.darkModeFlow.collect {
                isDarkMode.value = it
            }
        }
        if (userRepository.getFirebaseUser() != null) {
            getUserData()
            startAtLogin.value = false
        }
    }

    fun handleError(e: Exception) {
        _errorState.value = e.message ?: "Unknown error"
        logMessage("UserViewModel", "$e")
    }

    fun handleSuccess(message: String) {
        _successState.value = message
    }

    fun clearError() {
        _errorState.value = null
    }

    fun changeDarkMode(isDarkMode: Boolean?) {
        screenModelScope.launch {
            preferencesRepository.saveDarkMode(isDarkMode)
        }
    }

    fun getUserData() {
        screenModelScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }
            try {
                val user = userRepository.getUser(userRepository.getFirebaseUser()!!.uid)
                val groups = groupRepository.getGroups(user.groups)
                //val expenses = userRepository.getExpenses()
                val friends = friendsRepository.getFriends(user.friends)
                _state.update {
                    it.copy(
                        groups = groups,
                        friends = friends,
                        user = user
                    )
                }
//                logMessage("HomeViewModel", "getUserData: ${user.name}")
            } catch (e: Exception) {
                e.printStackTrace()
                logMessage("HomeViewModel", e.message.toString())
            }
            _state.update {
                it.copy(isLoading = false)
            }
        }
    }

    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFail: (Exception) -> Unit
    ) {
        screenModelScope.launch {
            try {
                _state.update {
                    it.copy(isLoading = true)
                }
                if (userRepository.signInWithEmailAndPassword(email, password) != null) {
                    //preferencesRepository.saveStartDestination(Screen.Home.route)
                    getUserData()
                    onSuccess()
                } else onFail(Exception("Failed to Log in"))
            } catch (e: Exception) {
                //Log.e("UserViewModel", "signInWithEmailAndPassword: ", e)
                onFail(handleAuthError(e))
                logMessage("UserViewModel: signInWithEmailAndPassword", e.message.toString())
            } finally {
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun signUpWithEmailAndPassword(
        email: String, password: String, name: String,
        onSuccess: () -> Unit,
        onFail: (Exception) -> Unit
    ) {
        screenModelScope.launch {
            try {
                _state.update {
                    it.copy(isLoading = true)
                }
                if (userRepository.signUpWithEmailAndPassword(email, password, name) != null) {
                    //preferencesRepository.saveStartDestination(Screen.Home.route)
                    getUserData()
                    onSuccess()
                } else onFail(Exception("Failed to Log in"))
            } catch (e: Exception) {
                onFail(handleAuthError(e))
                logMessage("UserViewModel: signUpWithEmailAndPassword", e.message.toString())
            } finally {
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun signInWithGoogle(
        result: Result<FirebaseUser?>,
        onSuccess: () -> Unit,
        onFail: (Exception) -> Unit
    ) {
        screenModelScope.launch {
            try {
                _state.update {
                    it.copy(isLoading = true)
                }

                if (result.getOrNull() != null) {
                    //preferencesRepository.saveStartDestination(Screen.Home.route)
                    getUserData()
                    onSuccess()
                } else {
                    onFail(Exception("Failed to Log in"))
                    logMessage(
                        "UserViewModel: signInWithGoogle",
                        "${result.exceptionOrNull()?.message}"
                    )
                }

            } catch (e: Exception) {
                onFail(Exception(e.message ?: "Unknown error"))
                logMessage("UserViewModel", e.message.toString())
                //Log.e("UserViewModel", "signUpWithEmailAndPassword: ", e)
            } finally {
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    private fun handleAuthError(e: Exception): Exception {
        return when (e.message) {
            "There is no user record corresponding to this identifier. The user may have been deleted.",
            "The password is invalid or the user does not have a password." -> {
                Exception("The email or password is invalid.")
            }

            "The email address is already in use by another account." -> {
                Exception(e.message)
            }

            else -> {
                Exception(e.message ?: "Unknown error")
            }
        }
    }

    fun signOut(onSignOut: () -> Unit) {
        screenModelScope.launch {
            try {
                _state.update {
                    it.copy(isLoading = true)
                }
                userRepository.signOut()
                if (userRepository.getFirebaseUser() == null) {
                    //preferencesRepository.saveStartDestination(Screen.Login.route)
                    onSignOut()
                    _state.update {
                        AppState(isLoading = false)
                    }
                }
            } catch (e: Exception) {
                logMessage("UserViewModel", e.message.toString())
                //Log.e("HomeViewModel", e.message.toString())
            }
        }
    }

    fun removeExpense(expenseId: String) {
        _state.update {
            it.copy(user = it.user.copy(expenses = it.user.expenses - expenseId))
        }
    }

    fun paidExpense(expenseId: String) {
        _state.update {
            it.copy(user = it.user.copy(expenses = it.user.expenses.mapValues { expense ->
                if (expense.key == expenseId) {
                    expense.value.copy(paid = true)
                } else {
                    expense.value
                }
            }))
        }
    }

    fun removeGroup(groupId: String) {
        _state.update {
            it.copy(groups = it.groups - groupId)
        }
    }

    fun addGroup(group: Group) {
        _state.update {
            it.copy(groups = it.groups + (group.id to group))
        }
    }

    fun saveExpense(expense: Expense) {
        _state.update {
            it.copy(user = it.user.copy(expenses = it.user.expenses + (expense.id to expense)))
        }
    }

    fun addFriend(friend: User) {
        _state.update {
            it.copy(friends = it.friends + (friend.uuid to friend))
        }
    }

    fun savePayment(expenseId: String, payment: Payment) {
        _state.update {
            it.copy(user = it.user.copy(expenses = it.user.expenses.mapValues { expense ->
                if (expense.key == expenseId) {
                    expense.value.copy(
                        payments = expense.value.payments + (payment.id to payment),
                        amountPaid = expense.value.amountPaid + payment.amount
                    )
                } else {
                    expense.value
                }
            }))
        }
    }

    fun deletePayment(expenseId: String, paymentId: String) {
        _state.update {
            it.copy(user = it.user.copy(expenses = it.user.expenses.mapValues { expense ->
                if (expense.key == expenseId) {
                    expense.value.copy(
                        payments = expense.value.payments - paymentId,
                        amountPaid = expense.value.amountPaid - expense.value.payments[paymentId]!!.amount
                    )
                } else {
                    expense.value
                }
            }))
        }
    }

    fun getGroupMembers(group: Group): List<User> {
        var users = listOf<User>()
            screenModelScope.launch {
                users = groupRepository.getUsers(group.users.values.map { it.id })
            }
        return users
    }

    fun saveGroupExpense(groupId: String, expense: GroupExpense) {
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        group.value.copy(expenses = group.value.expenses + (expense.id to expense))
                    } else {
                        group.value
                    }
                }
            )
        }
        expense.paidBy.entries.forEach { (payerId, amountPaid) ->
            val groupUser: GroupUser = _state.value.groups[groupId]?.users?.get(payerId)!!
            val newOwedMap = groupUser.owed.toMutableMap()
            expense.debtors.entries.forEach { (debtorId, debtorAmount) ->
                val debt = when (expense.splitMethod) {
                    SplitMethod.EQUALLY, SplitMethod.CUSTOM -> debtorAmount
                    SplitMethod.PERCENTAGES -> (debtorAmount * expense.amount) / 100
                }
                newOwedMap[debtorId] = (newOwedMap[debtorId] ?: 0.0) + debt
            }
            val payerOwed = expense.amount - when (expense.splitMethod) {
                SplitMethod.EQUALLY, SplitMethod.CUSTOM -> amountPaid
                SplitMethod.PERCENTAGES -> (amountPaid * expense.amount) / 100
            }
            _state.update {
                it.copy(
                    groups = it.groups.mapValues { group ->
                        if (group.key == groupId) {
                            group.value.copy(
                                users = group.value.users.mapValues { user ->
                                    if (user.key == payerId) {
                                        user.value.copy(
                                            owed = newOwedMap,
                                            totalOwed = user.value.totalOwed + payerOwed
                                        )
                                    } else {
                                        user.value
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
        expense.debtors.entries.forEach { (debtorId, amount) ->
            val groupUser: GroupUser = _state.value.groups[groupId]?.users?.get(debtorId)!!
            //groupUser.totalDebt += amount
            val newDebtsMap = groupUser.debts.toMutableMap()
            val totalDebt = when (expense.splitMethod) {
                SplitMethod.EQUALLY, SplitMethod.CUSTOM -> amount
                SplitMethod.PERCENTAGES -> (amount * expense.amount) / 100
            }
            expense.paidBy.keys.forEach { payerId ->
                newDebtsMap[payerId] = (newDebtsMap[payerId] ?: 0.0) + totalDebt
            }
            _state.update {
                it.copy(
                    groups = it.groups.mapValues { group ->
                        if (group.key == groupId) {
                            group.value.copy(
                                users = group.value.users.mapValues { user ->
                                    if (user.key == debtorId) {
                                        user.value.copy(
                                            debts = newDebtsMap,
                                            totalDebt = user.value.totalDebt + totalDebt
                                        )
                                    } else {
                                        user.value
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
    }

    fun updateGroupExpense(groupId: String, newExpense: GroupExpense, oldExpense: GroupExpense) {
        _state.update { currentUser ->
            // Actualiza el gasto del grupo
            val updatedGroups = currentUser.groups.mapValues { group ->
                if (group.key == groupId) {
                    group.value.copy(expenses = group.value.expenses + (newExpense.id to newExpense))
                } else {
                    group.value
                }
            }

            val updatedGroupsAfterPaidBy =
                newExpense.paidBy.entries.fold(updatedGroups) { groups, (payerId, amountPaid) ->
                    val groupUser = groups[groupId]?.users?.get(payerId) ?: return@fold groups
                    val newOwedMap = groupUser.owed.toMutableMap()

                    oldExpense.debtors.entries.forEach { (debtorId, debtorAmount) ->
                        val oldDebt = calculateDebt(oldExpense, debtorAmount)
                        newOwedMap[debtorId] = (newOwedMap[debtorId] ?: 0.0) - oldDebt
                    }

                    newExpense.debtors.entries.forEach { (debtorId, debtorAmount) ->
                        val newDebt = calculateDebt(newExpense, debtorAmount)
                        newOwedMap[debtorId] = (newOwedMap[debtorId] ?: 0.0) + newDebt
                    }

                    val newPayerOwed = newExpense.amount - calculateDebt(newExpense, amountPaid)
                    val oldPayerOwed =
                        oldExpense.amount - calculateDebt(oldExpense, oldExpense.paidBy[payerId]!!)

                    groups.mapValues { group ->
                        if (group.key == groupId) {
                            group.value.copy(
                                users = group.value.users.mapValues { user ->
                                    if (user.key == payerId) {
                                        user.value.copy(
                                            owed = newOwedMap,
                                            totalOwed = user.value.totalOwed + newPayerOwed - oldPayerOwed
                                        )
                                    } else {
                                        user.value
                                    }
                                }
                            )
                        } else {
                            group.value
                        }
                    }
                }

            // Procesa a los deudores
            val finalUpdatedGroups =
                newExpense.debtors.entries.fold(updatedGroupsAfterPaidBy) { groups, (debtorId, amount) ->
                    val groupUser = groups[groupId]?.users?.get(debtorId) ?: return@fold groups
                    val newDebtsMap = groupUser.debts.toMutableMap()

                    // Resta la vieja deuda
                    oldExpense.paidBy.entries.forEach { (payerId, payerAmount) ->
                        val oldDebt = calculateDebt(oldExpense, payerAmount)
                        newDebtsMap[payerId] = (newDebtsMap[payerId] ?: 0.0) - oldDebt
                    }

                    // Ajusta las deudas con los pagadores del nuevo gasto
                    newExpense.paidBy.entries.forEach { (payerId, payerAmount) ->
                        val newDebt = calculateDebt(newExpense, payerAmount)
                        newDebtsMap[payerId] = (newDebtsMap[payerId] ?: 0.0) + newDebt
                    }

                    groups.mapValues { group ->
                        if (group.key == groupId) {
                            group.value.copy(
                                users = group.value.users.mapValues { user ->
                                    if (user.key == debtorId) {
                                        user.value.copy(
                                            debts = newDebtsMap,
                                            totalDebt = user.value.totalDebt + amount - (oldExpense.debtors[debtorId]
                                                ?: 0.0)
                                        )
                                    } else {
                                        user.value
                                    }
                                }
                            )
                        } else {
                            group.value
                        }
                    }
                }

            // Devuelve el estado actualizado
            currentUser.copy(groups = finalUpdatedGroups)
        }
    }

    private fun calculateDebt(expense: GroupExpense, amount: Double): Double {
        return when (expense.splitMethod) {
            SplitMethod.EQUALLY, SplitMethod.CUSTOM -> amount
            SplitMethod.PERCENTAGES -> (amount * expense.amount) / 100
        }
    }

    fun removeGroupExpense(groupId: String, expense: GroupExpense) {
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        group.value.copy(expenses = group.value.expenses - expense.id)
                    } else {
                        group.value
                    }
                }
            )
        }
        expense.paidBy.entries.forEach { (payerId, amountPaid) ->
            val groupUser: GroupUser = _state.value.groups[groupId]?.users?.get(payerId)!!
            //groupUser.totalOwed += amountPaid
            val newOwedMap = groupUser.owed.toMutableMap()
            expense.debtors.entries.forEach { (debtorId, debtorAmount) ->
                val debt = when (expense.splitMethod) {
                    SplitMethod.EQUALLY, SplitMethod.CUSTOM -> debtorAmount
                    SplitMethod.PERCENTAGES -> (debtorAmount * amountPaid) / 100
                }
                newOwedMap[debtorId] =
                    if (newOwedMap[debtorId] == null) 0.0 else (newOwedMap[debtorId]!! - debt)
            }
            val payerOwed = expense.amount - when (expense.splitMethod) {
                SplitMethod.EQUALLY, SplitMethod.CUSTOM -> amountPaid
                SplitMethod.PERCENTAGES -> (amountPaid * expense.amount) / 100
            }
            _state.update {
                it.copy(
                    groups = it.groups.mapValues { group ->
                        if (group.key == groupId) {
                            group.value.copy(
                                users = group.value.users.mapValues { user ->
                                    if (user.key == payerId) {
                                        user.value.copy(
                                            owed = newOwedMap,
                                            totalOwed = user.value.totalOwed - payerOwed
                                        )
                                    } else {
                                        user.value
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
        expense.debtors.entries.forEach { (debtorId, amount) ->
            val groupUser: GroupUser = _state.value.groups[groupId]?.users?.get(debtorId)!!
            //groupUser.totalDebt += amount
            val newDebtsMap = groupUser.debts.toMutableMap()
            val totalDebt = when (expense.splitMethod) {
                SplitMethod.EQUALLY, SplitMethod.CUSTOM -> amount
                SplitMethod.PERCENTAGES -> (amount * groupUser.totalDebt) / 100
            }
            expense.paidBy.keys.forEach { payerId ->
                newDebtsMap[payerId] =
                    if (newDebtsMap[payerId] == null) 0.0 else (newDebtsMap[payerId]!! - totalDebt)
            }
            _state.update {
                it.copy(
                    groups = it.groups.mapValues { group ->
                        if (group.key == groupId) {
                            group.value.copy(
                                users = group.value.users.mapValues { user ->
                                    if (user.key == debtorId) {
                                        user.value.copy(
                                            debts = newDebtsMap,
                                            totalDebt = user.value.totalDebt - totalDebt
                                        )
                                    } else {
                                        user.value
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
    }

    fun getExpenseById(expenseId: String): Expense {
        return _state.value.user.expenses[expenseId] ?: Expense()
    }

    fun getGroupById(groupId: String): Group {
        return _state.value.groups[groupId] ?: Group()
    }

    fun getUUID(): String {
        return userRepository.getFirebaseUser()!!.uid
    }

    fun getGroupExpenseById(groupId: String, expenseId: String?): GroupExpense {
        return _state.value.groups[groupId]?.expenses?.get(expenseId) ?: GroupExpense()
    }
}