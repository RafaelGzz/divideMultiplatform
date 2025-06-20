package com.ragl.divide.data.models
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    override val id: String = "",
    override val amount: Double = 0.0,
    override val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
) : IPayment