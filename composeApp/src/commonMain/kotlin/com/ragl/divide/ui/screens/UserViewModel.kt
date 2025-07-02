package com.ragl.divide.ui.screens

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.AppLifecycleHandler
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupEvent
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.GroupPayment
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.data.repositories.FriendsRepository
import com.ragl.divide.data.repositories.GroupRepository
import com.ragl.divide.data.repositories.PreferencesRepository
import com.ragl.divide.data.repositories.UserRepository
import com.ragl.divide.data.services.AnalyticsService
import com.ragl.divide.data.services.GroupExpenseService
import com.ragl.divide.data.services.ScheduleNotificationService
import com.ragl.divide.ui.screens.groupProperties.PlatformImageUtils
import com.ragl.divide.ui.utils.Strings
import com.ragl.divide.ui.utils.logMessage
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class AppState(
    val isLoading: Boolean = false,
    val groups: Map<String, Group> = emptyMap(),
    val groupMembers: Map<String, List<UserInfo>> = emptyMap(),
    val friends: Map<String, UserInfo> = emptyMap(),
    val user: User = User(),
    val friendRequestsReceived: Map<String, UserInfo> = emptyMap(),
    val friendRequestsSent: Map<String, UserInfo> = emptyMap()
)

class UserViewModel(
    private var userRepository: UserRepository,
    private var friendsRepository: FriendsRepository,
    private val preferencesRepository: PreferencesRepository,
    private val groupRepository: GroupRepository,
    private val groupExpenseService: GroupExpenseService,
    private val scheduleNotificationService: ScheduleNotificationService,
    private val strings: Strings,
    private val analyticsService: AnalyticsService,
    private val appLifecycleHandler: AppLifecycleHandler
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
    var isInitializing = mutableStateOf(true)
        private set

    init {
        // Escuchar cambios en el modo oscuro
        screenModelScope.launch {
            preferencesRepository.darkModeFlow.collect {
                isDarkMode.value = it
            }
        }

        // Escuchar cambios en el ciclo de vida de la aplicación
        screenModelScope.launch {
            appLifecycleHandler.isAppInForeground.collect { isInForeground ->
                if (isInForeground && !startAtLogin.value && !isInitializing.value) {
                    logMessage("UserViewModel", "App regresó al foreground, actualizando datos")
                    getUserData()
                }
            }
        }

        // Inicialización de la aplicación
        screenModelScope.launch(Dispatchers.IO) {
            val startTime = Clock.System.now().toEpochMilliseconds()
            try {
                if (userRepository.getFirebaseUser() != null) {
                    if (userRepository.isEmailVerified()) {
                        startAtLogin.value = false
                        logMessage("UserViewModel", "User is verified, getting data")
                        getUserData()
                    } else {
                        userRepository.signOut()
                        startAtLogin.value = true
                        logMessage("UserViewModel", "User is not verified, signing out")
                    }
                } else {
                    startAtLogin.value = true
                    logMessage("UserViewModel", "User is not logged in")
                }
            } catch (e: Exception) {
                startAtLogin.value = true
                logMessage("UserViewModel", "Error during initialization: ${e.message}")
            } finally {
                isInitializing.value = false
            }
            val timeTaken = Clock.System.now().toEpochMilliseconds() - startTime
            logMessage("UserViewModel", "Initialization completed in $timeTaken ms")
        }
    }

    fun handleError(message: String?) {
        _errorState.value = message ?: strings.getUnknownError()
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

    suspend fun isFirstTime(): Boolean {
        return try {
            preferencesRepository.isFirstTimeFlow.first()
        } catch (e: Exception) {
            logMessage("UserViewModel", "Error checking first time: ${e.message}")
            false
        }
    }

    fun completeOnboarding() {
        screenModelScope.launch {
            preferencesRepository.setFirstTime(false)
            logMessage("UserViewModel", "Onboarding completed")
        }
    }

    fun getUserData() {
        screenModelScope.launch {
            showLoading()
            val startTime = Clock.System.now().toEpochMilliseconds()
            try {
                val user = userRepository.getUser(userRepository.getFirebaseUser()!!.uid)
                analyticsService.setUserProperties(user.uuid, user.name)
                val userInfo = UserInfo(user.uuid, user.name, user.photoUrl)

                logMessage("UserViewModel", "getFriends: ${user.friends.size}")
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

                _state.update {
                    it.copy(
                        groups = groups,
                        groupMembers = groupMembers,
                        friends = friends,
                        user = user,
                        friendRequestsReceived = friendRequestsReceived,
                        friendRequestsSent = friendRequestsSent
                    )
                }
            } catch (e: Exception) {
                analyticsService.logError(e, "Error al obtener datos del usuario")
                logMessage("UserViewModel", "getUserData: $e")
            } finally {
                hideLoading()
            }
            val timeTaken = Clock.System.now().toEpochMilliseconds() - startTime
            logMessage("UserViewModel", "getUserData completed in $timeTaken ms")
        }
    }

    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFail: (String) -> Unit
    ) {
        screenModelScope.launch {
            try {
                showLoading()
                if (userRepository.signInWithEmailAndPassword(email, password) != null) {
                    if (userRepository.isEmailVerified()) {
                        getUserData()
                        analyticsService.logEvent("login", mapOf(
                            "method" to "email"
                        ))
                        onSuccess()
                    } else {
                        onFail(strings.getEmailNotVerified())
                    }
                } else onFail(strings.getFailedToLogin())
            } catch (e: Exception) {
                analyticsService.logError(e, "Error en login con email")
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
                    analyticsService.logEvent("sign_up", mapOf(
                        "method" to "email",
                        "email" to email
                    ))
                    userRepository.signOut()
                    handleSuccess(strings.getVerificationEmailSent())
                } else handleError(strings.getFailedToLogin())
            } catch (e: Exception) {
                analyticsService.logError(e, "Error en registro con email")
                handleError(handleAuthError(e))
                logMessage("UserViewModel: signUpWithEmailAndPassword", e.message.toString())
            } finally {
                hideLoading()
            }
        }
    }

    fun signInWithGoogle(
        result: Result<FirebaseUser?>,
        onSuccess: () -> Unit
    ) {
        screenModelScope.launch {
            try {
                showLoading()
                val firebaseUser = result.getOrNull()
                if (firebaseUser != null) {
                    val checkedUser = userRepository.getUser(firebaseUser.uid)
                    if (checkedUser.uuid.isEmpty()) {
                        val newUser = userRepository.createUserInDatabase()
                        analyticsService.logEvent("sign_up", mapOf(
                            "method" to "google",
                            "email" to newUser.email
                        ))
                    } else {
                        analyticsService.logEvent("login", mapOf(
                            "method" to "google",
                            "email" to checkedUser.email
                        ))
                    }
                    onSuccess()
                } else {
                    handleError(strings.getFailedToLogin())
                    analyticsService.logError(result.exceptionOrNull()!!, "Error en login con google")
                    logMessage(
                        "UserViewModel: signInWithGoogle",
                        "${result.exceptionOrNull()?.message}"
                    )
                }

            } catch (e: Exception) {
                analyticsService.logError(e, "Error en login con google")
                logMessage("UserViewModel", "${e.message}")
            } finally {
                hideLoading()
            }
        }
    }

    private fun handleAuthError(e: Exception): String {
        return when {
            e.message?.contains("no user record") == true ||
                    e.message?.contains("password is invalid") == true -> {
                strings.getEmailPasswordInvalid()
            }

            e.message?.contains("email address is already in use") == true -> {
                strings.getEmailAlreadyInUse()
            }

            e.message?.contains("unusual activity") == true -> {
                strings.getUnusualActivity()
            }

            else -> {
                e.message ?: strings.getUnknownError()
            }
        }
    }

    fun signOut(onSignOut: () -> Unit) {
        screenModelScope.launch {
            try {
                showLoading()
                scheduleNotificationService.cancelAllNotifications()
                userRepository.signOut()
                if (userRepository.getFirebaseUser() == null) {
                    onSignOut()
                    _state.update { AppState() }
                }
            } catch (e: Exception) {
                logMessage("UserViewModel", e.message.toString())
            } finally {
                hideLoading()
            }
        }
    }

    fun removeExpense(expenseId: String) {
        _state.update {
            it.copy(user = it.user.copy(expenses = it.user.expenses - expenseId))
        }
    }

    fun updatePaidExpense(expenseId: String, paid: Boolean) {
        _state.update {
            it.copy(user = it.user.copy(expenses = it.user.expenses.mapValues { expense ->
                if (expense.key == expenseId) {
                    expense.value.copy(paid = paid)
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

    fun saveGroup(group: Group) {
        _state.update {
            it.copy(groups = it.groups + (group.id to group))
        }
        group.events.values.map {
            if (!it.settled) {
                recalculateEventDebts(group.id, it.id)
            }
        }
    }

    fun saveExpense(expense: Expense) {
        _state.update {
            it.copy(user = it.user.copy(expenses = it.user.expenses + (expense.id to expense)))
        }
    }

    fun addFriend(friend: UserInfo) {
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
                        amountPaid = expense.value.amountPaid + payment.amount,
                        paid = expense.value.amountPaid + payment.amount == expense.value.amount
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
                        amountPaid = expense.value.amountPaid - expense.value.payments[paymentId]!!.amount,
                        paid = false
                    )
                } else {
                    expense.value
                }
            }))
        }
    }

    fun saveGroupExpense(groupId: String, expense: GroupExpense) {
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        if (expense.eventId.isEmpty())
                            group.value.copy(expenses = group.value.expenses + (expense.id to expense))
                        else
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

    fun removeGroupExpense(groupId: String, expense: GroupExpense) {
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        if (expense.eventId.isEmpty())
                            group.value.copy(expenses = group.value.expenses - expense.id)
                        else
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

    fun getExpenseById(expenseId: String): Expense {
        return _state.value.user.expenses[expenseId] ?: Expense()
    }

    fun getGroupById(id: String): Group {
        return _state.value.groups[id] ?: Group()
    }

    fun getGroupExpenseById(
        groupId: String,
        expenseId: String?,
        eventId: String? = null
    ): GroupExpense {
        return if (eventId != null) {
            _state.value.groups[groupId]?.events?.get(eventId)?.expenses?.get(expenseId)
                ?: GroupExpense()
        } else
            _state.value.groups[groupId]?.expenses?.get(expenseId) ?: GroupExpense()
    }

    fun getGroupPaymentById(groupId: String, paymentId: String?, eventId: String? = null): GroupPayment {
        return if (eventId != null) {
            _state.value.groups[groupId]?.events?.get(eventId)?.payments?.get(paymentId)
                ?: GroupPayment()
        } else
            _state.value.groups[groupId]?.payments?.get(paymentId) ?: GroupPayment()
    }

    fun getUUID(): String {
        return _state.value.user.uuid
    }

    /**
     * Consolida las deudas de eventos usando el servicio interno
     */
    fun consolidateDebtsFromEvents(eventsMap: Map<String, GroupEvent>): Map<String, Map<String, Double>> {
        return groupExpenseService.consolidateDebtsFromEventsMap(eventsMap)
    }

    fun saveGroupPayment(groupId: String, savedPayment: GroupPayment) {
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        if (savedPayment.eventId.isEmpty())
                            group.value.copy(payments = group.value.payments + (savedPayment.id to savedPayment))
                        else
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

    fun deleteGroupPayment(groupId: String, payment: GroupPayment) {
        _state.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        if (payment.eventId.isEmpty())
                            group.value.copy(payments = group.value.payments - payment.id)
                        else
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

    fun resendVerificationEmail() {
        screenModelScope.launch {
            try {
                showLoading()
                userRepository.sendEmailVerification()
                handleSuccess(strings.getVerificationEmailSent())
                startCountdown()
            } catch (e: Exception) {
                handleError(e.message ?: strings.getUnknownError())
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
            handleError(e.message ?: strings.getUnknownError())
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

    fun updateUserName(newName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                showLoading()
                val trimmedName = newName.trim()
                
                if (trimmedName.isBlank()) {
                    onError(strings.getUsernameEmpty())
                    return@launch
                }
                
                val success = userRepository.updateUserName(trimmedName)
                if (success) {
                    _state.update {
                        it.copy(user = it.user.copy(name = trimmedName))
                    }
                    onSuccess()
                } else {
                    onError(strings.getUnknownError())
                }
            } catch (e: Exception) {
                logMessage("UserViewModel", e.toString())
                onError(e.message ?: strings.getUnknownError())
            } finally {
                hideLoading()
            }
        }
    }

    fun sendFriendRequest(friend: UserInfo) {
        screenModelScope.launch {
            try {
                showLoading()
                if (friendsRepository.sendFriendRequest(friend.uuid)) {
                    _state.update {
                        it.copy(friendRequestsSent = it.friendRequestsSent + (friend.uuid to friend))
                    }
                    handleSuccess(strings.getFriendRequestSent())
                } else {
                    handleError(strings.getFailedToSendFriendRequest())
                }
            } catch (e: Exception) {
                handleError(e.message ?: strings.getUnknownError())
            } finally {
                hideLoading()
            }
        }
    }

    fun acceptFriendRequest(friend: UserInfo) {
        screenModelScope.launch {
            try {
                showLoading()
                if (friendsRepository.acceptFriendRequest(friend.uuid)) {
                    _state.update {
                        it.copy(
                            friendRequestsReceived = it.friendRequestsReceived - friend.uuid,
                            friends = it.friends + (friend.uuid to friend)
                        )
                    }
                    handleSuccess(strings.getFriendRequestAccepted())
                } else {
                    handleError(strings.getFailedToAcceptFriendRequest())
                }
            } catch (e: Exception) {
                handleError(e.message ?: strings.getUnknownError())
            } finally {
                hideLoading()
            }
        }
    }

    fun rejectFriendRequest(friend: UserInfo) {
        screenModelScope.launch {
            try {
                showLoading()
                if (friendsRepository.rejectFriendRequest(friend.uuid)) {
                    _state.update {
                        it.copy(friendRequestsReceived = it.friendRequestsReceived - friend.uuid)
                    }
                    handleSuccess(strings.getFriendRequestRejected())
                } else {
                    handleError(strings.getFailedToRejectFriendRequest())
                }
            } catch (e: Exception) {
                handleError(e.message ?: strings.getUnknownError())
            } finally {
                hideLoading()
            }
        }
    }

    fun cancelFriendRequest(friend: UserInfo) {
        screenModelScope.launch {
            try {
                showLoading()
                if (friendsRepository.cancelFriendRequest(friend.uuid)) {
                    _state.update {
                        it.copy(friendRequestsSent = it.friendRequestsSent - friend.uuid)
                    }
                    handleSuccess(strings.getFriendRequestCanceled())
                } else {
                    handleError(strings.getFailedToCancelFriendRequest())
                }
            } catch (e: Exception) {
                handleError(e.message ?: strings.getUnknownError())
            } finally {
                hideLoading()
            }
        }
    }

    fun removeFriend(friend: UserInfo) {
        screenModelScope.launch {
            try {
                showLoading()
                if (friendsRepository.removeFriend(friend.uuid)) {
                    _state.update {
                        it.copy(friends = it.friends - friend.uuid)
                    }
                    handleSuccess(strings.getFriendRemoved())
                } else {
                    handleError(strings.getFailedToRemoveFriend())
                }
            } catch (e: Exception) {
                handleError(e.message ?: strings.getUnknownError())
            } finally {
                hideLoading()
            }
        }
    }

    fun getGroupMembers(groupId: String): List<UserInfo> {
        return _state.value.groupMembers[groupId] ?: emptyList()
    }

    fun getGroupMembersWithGuests(groupId: String): List<UserInfo> {
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

    fun setGroupMembers(group: Group) {
        screenModelScope.launch {
            val currentGroupMembers = _state.value.groupMembers[group.id] ?: emptyList()
            val currentGroupMembersIds = currentGroupMembers.map { it.uuid }
            val newGroupUserIds = group.users.values.toList()

            val missingUserIds = newGroupUserIds.filter { it !in currentGroupMembersIds }
            if (missingUserIds.isNotEmpty()) {
                val newGroupMembers = friendsRepository.getFriends(missingUserIds)
                val updatedGroupMembers = currentGroupMembers + newGroupMembers.values

                _state.update { state ->
                    state.copy(
                        groupMembers = state.groupMembers + (group.id to updatedGroupMembers)
                    )
                }
            }
        }
    }

    // Métodos para eventos
    fun getEventById(groupId: String, eventId: String?): GroupEvent {
        return _state.value.groups[groupId]?.events?.get(eventId) ?: GroupEvent()
    }

    fun getEvents(groupId: String): Map<String, GroupEvent> {
        return _state.value.groups[groupId]?.events ?: emptyMap()
    }

    fun saveEvent(groupId: String, event: GroupEvent) {
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

    fun deleteEvent(groupId: String, eventId: String) {
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

    fun settleEvent(groupId: String, eventId: String) {
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

    fun reopenEvent(groupId: String, eventId: String) {
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

    private fun recalculateEventDebts(groupId: String, eventId: String) {
        val group = _state.value.groups[groupId] ?: return
        val simplifyDebts = group.simplifyDebts

        val expensesToSettle = mutableListOf<String>()
        val paymentsToSettle = mutableListOf<String>()

        val expenses = group.events[eventId]?.expenses?.values?.toList() ?: emptyList()
        val payments = group.events[eventId]?.payments?.values?.toList() ?: emptyList()

        val currentDebts = groupExpenseService.calculateDebts(
            expenses,
            payments,
            simplifyDebts,
            expensesToSettle,
            paymentsToSettle
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

    fun updateGroupInState(groupId: String, updatedGroup: Group) {
        _state.update { currentState ->
            currentState.copy(
                groups = currentState.groups + (groupId to updatedGroup)
            )
        }
        logMessage("UserViewModel", "Updated group $groupId in state")
    }

    fun updateEventInState(groupId: String, eventId: String, updatedEvent: GroupEvent) {
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
        logMessage("UserViewModel", "Updated event $eventId in group $groupId in state")
    }
}