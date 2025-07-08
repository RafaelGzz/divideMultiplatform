package com.ragl.divide.data.models

import kotlinx.serialization.Serializable
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
    val type: PaymentType = PaymentType.CASH,
    val image: String = "",
) : IPayment

@Serializable
enum class PaymentType {
    CASH,
    TRANSFER,
    OTHER,
    LOAN_CASH,
    LOAN_TRANSFER,
    LOAN_OTHER
}