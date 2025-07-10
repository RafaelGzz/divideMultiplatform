package com.ragl.divide.data.models

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.DollarSign
import compose.icons.fontawesomeicons.solid.ExchangeAlt
import compose.icons.fontawesomeicons.solid.MoneyBill
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.cash
import dividemultiplatform.composeapp.generated.resources.other_payment
import dividemultiplatform.composeapp.generated.resources.transfer
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class EventPayment @OptIn(ExperimentalTime::class) constructor(
    override val id: String = "",
    override val amount: Double = 0.0,
    override val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val eventId: String = "",
    val from: String = "",
    val to: String = "",
    val description: String = "",
    val isLoan: Boolean = false,
    val image: String = "",
) : IPayment

@Serializable
enum class PaymentType(val isLoan: Boolean = false) {
    CASH,
    TRANSFER,
    OTHER,
    LOAN_CASH(true),
    LOAN_TRANSFER(true),
    LOAN_OTHER(true);

    @Composable
    fun getName(): String{
        return when(this){
            CASH, LOAN_CASH -> stringResource( Res.string.cash)
            TRANSFER, LOAN_TRANSFER -> stringResource( Res.string.transfer)
            OTHER, LOAN_OTHER -> stringResource( Res.string.other_payment)
        }
    }

    fun getIcon(): ImageVector{
        return when(this){
            CASH, LOAN_CASH -> FontAwesomeIcons.Solid.MoneyBill
            TRANSFER, LOAN_TRANSFER -> FontAwesomeIcons.Solid.ExchangeAlt
            OTHER, LOAN_OTHER -> FontAwesomeIcons.Solid.DollarSign
        }
    }
}