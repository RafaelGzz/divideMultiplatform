package com.ragl.divide.ui.screens

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import com.ragl.divide.data.repositories.FriendsRepository
import com.ragl.divide.data.repositories.GroupRepository
import com.ragl.divide.data.repositories.PreferencesRepository
import com.ragl.divide.data.repositories.UserRepository
import com.ragl.divide.data.services.GroupExpenseService
import com.ragl.divide.ui.screens.groupProperties.PlatformImageUtils
import com.ragl.divide.ui.utils.Strings
import com.ragl.divide.ui.utils.logMessage
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
    private val groupRepository: GroupRepository,
    private val groupExpenseService: GroupExpenseService,
    private val strings: Strings
) : ScreenModel {

    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()

    private val _successState = MutableStateFlow<String?>(null)
    val successState = _successState.asStateFlow()

    private val _countdown = MutableStateFlow(0)
    val countdown = _countdown.asStateFlow()

    var isDarkMode = MutableStateFlow<String?>(null)

    var startAtLogin = mutableStateOf(true)
        private set

    init {
        screenModelScope.launch {
            preferencesRepository.darkModeFlow.collect {
                isDarkMode.value = it
            }
        }
        runBlocking {
            if (userRepository.getFirebaseUser() != null) {
                if (userRepository.isEmailVerified()) {
                    getUserData()
                    startAtLogin.value = false
                } else {
                    userRepository.signOut()
                    startAtLogin.value = true
                }
            }
        }
    }

    fun handleError(e: Exception) {
        _errorState.value = e.message ?: strings.getUnknownError()
        logMessage("UserViewModel", "$e")
    }

    fun handleSuccess(message: String) {
        _successState.value = message
    }

    fun clearError() {
        _errorState.value = null
    }

    fun showLoading() {
        _state.update {
            it.copy(isLoading = true)
        }
    }

    fun hideLoading() {
        _state.update {
            it.copy(isLoading = false)
        }
    }

    fun changeDarkMode(isDarkMode: Boolean?) {
        screenModelScope.launch {
            preferencesRepository.saveDarkMode(isDarkMode)
        }
    }

    fun getUserData() {
        screenModelScope.launch {
            showLoading()
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
            hideLoading()
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
                showLoading()
                if (userRepository.signInWithEmailAndPassword(email, password) != null) {
                    if (userRepository.isEmailVerified()) {
                        getUserData()
                        onSuccess()
                    } else {
                        onFail(Exception(strings.getEmailNotVerified()))
                    }
                } else onFail(Exception(strings.getFailedToLogin()))
            } catch (e: Exception) {
                onFail(handleAuthError(e))
                logMessage("UserViewModel: signInWithEmailAndPassword", e.message.toString())
            } finally {
                hideLoading()
            }
        }
    }

    fun signUpWithEmailAndPassword(
        email: String, password: String, name: String
    ) {
        screenModelScope.launch {
            try {
                showLoading()
                if (userRepository.signUpWithEmailAndPassword(email, password, name) != null) {
                    userRepository.signOut()
                    handleSuccess(strings.getVerificationEmailSent())
                } else handleError(Exception(strings.getFailedToLogin()))
            } catch (e: Exception) {
                handleError(handleAuthError(e))
                logMessage("UserViewModel: signUpWithEmailAndPassword", e.message.toString())
            } finally {
                hideLoading()
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
                showLoading()

                val firebaseUser = result.getOrNull()
                if (firebaseUser != null) {
                    val checkedUser = userRepository.getUser(firebaseUser.uid)
                    if (checkedUser.uuid.isEmpty()) {
                        userRepository.createUserInDatabase()
                    }
                    getUserData()
                    onSuccess()
                } else {
                    onFail(Exception(strings.getFailedToLogin()))
                    logMessage(
                        "UserViewModel: signInWithGoogle",
                        "${result.exceptionOrNull()?.message}"
                    )
                }

            } catch (e: Exception) {
                onFail(Exception(e.message ?: strings.getUnknownError()))
                logMessage("UserViewModel", e.message.toString())
                //Log.e("UserViewModel", "signUpWithEmailAndPassword: ", e)
            } finally {
                hideLoading()
            }
        }
    }

    private fun handleAuthError(e: Exception): Exception {
        return when {
            e.message?.contains("no user record") == true ||
                    e.message?.contains("password is invalid") == true -> {
                Exception(strings.getEmailPasswordInvalid())
            }

            e.message?.contains("email address is already in use") == true -> {
                Exception(strings.getEmailAlreadyInUse())
            }

            e.message?.contains("unusual activity") == true -> {
                Exception(strings.getUnusualActivity())
            }

            else -> {
                Exception(e.message ?: strings.getUnknownError())
            }
        }
    }

    fun signOut(onSignOut: () -> Unit) {
        screenModelScope.launch {
            try {
                showLoading()
                userRepository.signOut()
                if (userRepository.getFirebaseUser() == null) {
                    //preferencesRepository.saveStartDestination(Screen.Login.route)
                    onSignOut()
                    hideLoading()
                }
            } catch (e: Exception) {
                logMessage("UserViewModel", e.message.toString())
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
        recalculateDebts(group.id)
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

    private fun recalculateDebts(groupId: String) {
        val group = _state.value.groups[groupId] ?: return

        val expensesToSettle = mutableListOf<String>()
        val paymentsToSettle = mutableListOf<String>()
        val currentDebts = groupExpenseService.calculateDebts(
            group.expenses.values,
            group.payments.values,
            group.simplifyDebts,
            expensesToSettle,
            paymentsToSettle
        )
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        group.value.copy(
                            currentDebts = currentDebts,
                            expenses = if (expensesToSettle.isNotEmpty()) {
                                group.value.expenses.mapValues { expense ->
                                    if (expense.key in expensesToSettle) {
                                        expense.value.copy(settled = true)
                                    } else {
                                        expense.value
                                    }
                                }
                            } else {
                                group.value.expenses
                            },
                            payments = if (paymentsToSettle.isNotEmpty()) {
                                group.value.payments.mapValues { payment ->
                                    if (payment.key in paymentsToSettle) {
                                        payment.value.copy(settled = true)
                                    } else {
                                        payment.value
                                    }
                                }
                            } else {
                                group.value.payments
                            }
                        )
                    } else {
                        group.value
                    }
                }
            )
        }
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
        recalculateDebts(groupId)
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
        recalculateDebts(groupId)
    }

    fun getExpenseById(expenseId: String): Expense {
        return _state.value.user.expenses[expenseId] ?: Expense()
    }

    fun getGroupById(id: String): Group {
        return _state.value.groups[id] ?: Group()
    }

    fun getGroupExpenseById(groupId: String, expenseId: String?): GroupExpense {
        return _state.value.groups[groupId]?.expenses?.get(expenseId) ?: GroupExpense()
    }

    fun getGroupPaymentById(groupId: String, paymentId: String?): Payment {
        return _state.value.groups[groupId]?.payments?.get(paymentId) ?: Payment()
    }

    fun getUUID(): String {
        return userRepository.getFirebaseUser()!!.uid
    }

    fun saveGroupPayment(groupId: String, savedPayment: Payment) {
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        group.value.copy(payments = group.value.payments + (savedPayment.id to savedPayment))
                    } else {
                        group.value
                    }
                }
            )
        }
        recalculateDebts(groupId)
    }

    fun deleteGroupPayment(groupId: String, paymentId: String) {
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        group.value.copy(payments = group.value.payments - paymentId)
                    } else {
                        group.value
                    }
                }
            )
        }
        recalculateDebts(groupId)
    }

    fun resendVerificationEmail() {
        screenModelScope.launch {
            try {
                showLoading()
                userRepository.sendEmailVerification()
                handleSuccess(strings.getVerificationEmailSent())
                startCountdown()
            } catch (e: Exception) {
                handleError(Exception(e.message ?: strings.getUnknownError()))
            } finally {
                hideLoading()
            }
        }
    }

    private fun startCountdown() {
        screenModelScope.launch {
            _countdown.value = 300
            while (_countdown.value > 0) {
                delay(1000)
                _countdown.value--
            }
        }
    }

    suspend fun isEmailVerified(): Boolean {
        var res = false
        try {
            showLoading()
            res = userRepository.isEmailVerified()
        } catch (e: Exception) {
            handleError(Exception(e.message ?: strings.getUnknownError()))
        } finally {
            hideLoading()
        }
        return res
    }

    fun updateProfileImage(imagePath: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                showLoading()
                val imageFile = PlatformImageUtils.createFirebaseFile(imagePath)
                
                if (imageFile != null) {
                    val downloadUrl = userRepository.saveProfilePhoto(imageFile)
                    _state.update {
                        it.copy(user = it.user.copy(photoUrl = downloadUrl))
                    }
                    onSuccess()
                } else {
                    logMessage("UserViewModel", "Could not process image")
                    onError(strings.getCouldNotProcessImage())
                }
            } catch (e: Exception) {
                logMessage("UserViewModel", e.toString())
                onError(e.message ?: strings.getUnknownError())
            } finally {
                hideLoading()
            }
        }
    }
}