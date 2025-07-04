package com.ragl.divide.data.models
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class Payment @OptIn(ExperimentalTime::class) constructor(
    override val id: String = "",
    override val amount: Double = 0.0,
    override val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
) : IPayment