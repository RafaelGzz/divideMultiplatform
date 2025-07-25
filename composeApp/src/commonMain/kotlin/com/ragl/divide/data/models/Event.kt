package com.ragl.divide.data.models

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class Event @OptIn(ExperimentalTime::class) constructor(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: Category = Category.GENERAL,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val expenses: Map<String, EventExpense> = emptyMap(),
    val payments: Map<String, EventPayment> = emptyMap(),
    val settled: Boolean = false,
    val currentDebts: Map<String, Map<String, Double>> = emptyMap(),
)