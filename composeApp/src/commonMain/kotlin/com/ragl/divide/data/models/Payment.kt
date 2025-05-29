package com.ragl.divide.data.models
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val id: String = "",
    val amount: Double = 0.0,
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val from: String = "",
    val to: String = "",
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
    val settled: Boolean = false,
    val eventId: String = "",
)