package com.ragl.divide.data.models
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val id: String = "",
    val amount: Double = 0.0,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val from: String = "",
    val to: String = "",
    val image: String = "",
    val type: PaymentType = PaymentType.CASH,
    val eventId: String = "",
)

@Serializable
enum class PaymentType {
    CASH,
    TRANSFER,
    OTHER,
    LOAN_CASH,      // Préstamo en efectivo
    LOAN_TRANSFER,  // Préstamo por transferencia
    LOAN_OTHER      // Préstamo por otro medio
}

// Funciones de extensión para facilitar el uso
fun Payment.isLoanPayment(): Boolean = type.isLoanType()

fun PaymentType.isLoanType(): Boolean = when (this) {
    PaymentType.LOAN_CASH, PaymentType.LOAN_TRANSFER, PaymentType.LOAN_OTHER -> true
    else -> false
}

fun Payment.getDisplayType(): String = when {
    type.isLoanType() -> "Préstamo"
    else -> "Pago"
}