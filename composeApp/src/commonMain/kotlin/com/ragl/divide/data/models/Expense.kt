package com.ragl.divide.data.models

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class Expense @OptIn(ExperimentalTime::class) constructor(
    override val id: String = "",
    override val title: String = "",
    override val category: Category = Category.GENERAL,
    override val amount: Double = 0.0,
    override val notes: String = "",
    override val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val amountPaid: Double = 0.0,
    val reminders: Boolean = false,
    val frequency: Frequency = Frequency.DAILY,
    val startingDate: Long = Clock.System.now().toEpochMilliseconds(),
    val payments: Map<String, Payment> = emptyMap(),
    val paid: Boolean = false
): IExpense

