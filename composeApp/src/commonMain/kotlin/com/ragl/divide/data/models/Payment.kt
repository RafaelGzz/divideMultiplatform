package com.ragl.divide.data.models
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val id: String = "",
    val amount: Double = 0.0,
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val issuer: String = "",
    val receiver: String = "",
    val expensesDeducted: Map<String, Double> = emptyMap()
)