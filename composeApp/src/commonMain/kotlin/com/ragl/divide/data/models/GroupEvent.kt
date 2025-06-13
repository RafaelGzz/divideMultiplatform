package com.ragl.divide.data.models

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class GroupEvent(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: Category = Category.GENERAL,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val expenses: Map<String, GroupExpense> = emptyMap(),
    val payments: Map<String, Payment> = emptyMap(),
    val settled: Boolean = false,
    val currentDebts: Map<String, Map<String, Double>> = emptyMap(),
)