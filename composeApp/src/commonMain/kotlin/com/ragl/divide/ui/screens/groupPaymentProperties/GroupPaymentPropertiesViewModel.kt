package com.ragl.divide.ui.screens.groupPaymentProperties

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import com.ragl.divide.data.repositories.GroupRepository
import com.ragl.divide.ui.utils.Strings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class GroupPaymentPropertiesViewModel(
    private val groupRepository: GroupRepository,
    private val strings: Strings
) : ScreenModel {

    private val _group = MutableStateFlow(Group())
    val group = _group.asStateFlow()

    private val _payment = MutableStateFlow(Payment())
    val payment = _payment.asStateFlow()

    var isUpdate = mutableStateOf(false)
    var userId by mutableStateOf("")

    var amount by mutableStateOf("")
        private set
    var amountError by mutableStateOf("")
        private set
    var from by mutableStateOf(User())
        private set
    var to by mutableStateOf(User())
        private set

    fun updateAmount(amount: String) {
        this.amount = amount
    }

    fun updateFrom(user: User) {
        from = user
    }

    fun updateTo(user: User) {
        to = user
    }

    fun setGroupAndPayment(
        group: Group,
        userId: String,
        members: List<User>,
        payment: Payment
    ) {
        screenModelScope.launch {
            if (payment.id.isNotEmpty()) {
                isUpdate.value = true
                _payment.update { payment }
                amount = payment.amount.let { if (it == 0.0) "" else it.toString() }
                from = members.firstOrNull { it.uuid == payment.from } ?: User()
                to = members.firstOrNull { it.uuid == payment.to } ?: User()
            } else {
                from = members.first { it.uuid == userId }
                to = members.firstOrNull { it.uuid != userId } ?: User()
            }
            _group.update { group }
        }
        this.userId = userId
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

    fun savePayment(
        onSuccess: (Payment) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            if (validateAmount()) {
                val payment = _payment.value.copy(
                    amount = amount.toDouble(),
                    from = from.uuid,
                    to = to.uuid,
                    date = Clock.System.now().toEpochMilliseconds()
                )

                screenModelScope.launch {
                    val savedPayment =
                        groupRepository.savePayment(
                            groupId = _group.value.id,
                            payment = payment
                        )
                    onSuccess(savedPayment)
                }
            }
        } catch (e: Exception) {
            onError(e.message.toString())
        }
    }
} 