package com.ragl.divide.presentation.screens.eventPaymentProperties

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.EventPayment
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.components.DebtInfo
import com.ragl.divide.presentation.utils.Strings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class EventPaymentPropertiesViewModel(
    private val groupRepository: GroupRepository,
    private val userStateHolder: UserStateHolder,
    private val strings: Strings
) : ScreenModel {

    private val _payment = MutableStateFlow(EventPayment())
    val payment = _payment.asStateFlow()

    var isUpdate by mutableStateOf(false)
    private var userId by mutableStateOf("")
    private var groupId by mutableStateOf("")
    private var eventId by mutableStateOf<String?>(null)

    val descriptionCharacterLimit = 100

    var description by mutableStateOf("")
        private set
    var descriptionError by mutableStateOf("")
        private set
    var amount by mutableStateOf("")
        private set
    var amountError by mutableStateOf("")
        private set
    var from by mutableStateOf(UserInfo())
        private set
    var to by mutableStateOf(UserInfo())
        private set
    var members by mutableStateOf(listOf<UserInfo>())
        private set

    fun updateDescription(description: String) {
        this.description = description
    }

    fun updateAmount(amount: String) {
        this.amount = amount
    }

    fun updateFrom(user: UserInfo) {
        from = user
    }

    fun updateTo(user: UserInfo) {
        to = user
    }

    fun updateIsLoan(isLoan: Boolean) {
        _payment.update { it.copy(isLoan = isLoan) }
    }

    fun setGroupAndPayment(
        groupId: String,
        paymentId: String? = null,
        eventId: String,
        currentDebtInfo: DebtInfo? = null
    ) {
        val members = userStateHolder.getGroupMembersWithGuests(groupId)
        val payment = userStateHolder.getEventPaymentById(groupId, paymentId, eventId)

        screenModelScope.launch {
            if (payment.id.isNotEmpty()) {
                isUpdate = true
                _payment.update { payment }
                description = payment.description
                amount = payment.amount.let { if (it == 0.0) "" else it.toString() }
                from = members.firstOrNull { it.uuid == payment.from } ?: UserInfo()
                to = members.firstOrNull { it.uuid == payment.to } ?: UserInfo()
            } else {
                if (currentDebtInfo != null) {
                    from =
                        members.firstOrNull { it.uuid == currentDebtInfo.fromUserId } ?: UserInfo()
                    to = members.firstOrNull { it.uuid == currentDebtInfo.toUserId } ?: UserInfo()
                    amount = currentDebtInfo.amount.toString()
                } else {
                    from = members.firstOrNull() ?: UserInfo()
                    to = members.firstOrNull() ?: UserInfo()
                }
            }
        }
        this.members = members
        this.eventId = eventId
        this.groupId = groupId
        this.userId = members.firstOrNull()?.uuid ?: ""
    }

    fun validateAmount(): Boolean {
        if (amount.isEmpty()) {
            this.amountError = strings.getAmountRequired()
            return false
        }

        val amountDouble = amount.toDoubleOrNull() ?: run {
            this.amountError = strings.getInvalidAmount()
            return false
        }

        if (amountDouble <= 0) {
            this.amountError = strings.getAmountMustBeGreater()
            return false
        }

        this.amountError = ""
        return true
    }

    fun validateDescription(): Boolean {
        if (description.trim().length > descriptionCharacterLimit) {
            this.descriptionError = strings.getDescriptionTooLong()
            return false
        }
        this.descriptionError = ""
        return true
    }

    fun savePayment(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            if (validateAmount().and(validateDescription())) {
                val payment = _payment.value.copy(
                    amount = amount.toDouble(),
                    from = from.uuid,
                    to = to.uuid,
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                    description = description.trim(),
                    eventId = eventId ?: "",
                )

                screenModelScope.launch {
                    val savedPayment =
                        groupRepository.saveEventPayment(
                            groupId = groupId,
                            payment = payment
                        )
                    userStateHolder.saveGroupPayment(groupId, savedPayment)
                    onSuccess()
                }
            }
        } catch (e: Exception) {
            onError(e.message.toString())
        }
    }
}