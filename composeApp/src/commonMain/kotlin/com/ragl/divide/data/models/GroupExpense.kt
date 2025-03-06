package com.ragl.divide.data.models

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class GroupExpense(
    override val id: String = "",
    override val title: String = "",
    override val category: Category = Category.GENERAL,
    override val amount: Double = 0.0,
    override val notes: String = "",
    override val addedDate: Long = Clock.System.now().toEpochMilliseconds(),
    val splitMethod: Method,
    val paidBy: Map<String, Double> = emptyMap(),
    val debtors: Map<String, Double> = emptyMap(),
    val amountPaid: Map<String, Double> = emptyMap(),
): IExpense