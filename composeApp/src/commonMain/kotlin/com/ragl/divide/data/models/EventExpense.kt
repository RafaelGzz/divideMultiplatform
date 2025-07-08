package com.ragl.divide.data.models

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class EventExpense @OptIn(ExperimentalTime::class) constructor(
    override val id: String = "",
    override val title: String = "",
    override val category: Category = Category.GENERAL,
    override val amount: Double = 0.0,
    override val notes: String = "",
    override val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val splitMethod: SplitMethod = SplitMethod.EQUALLY,
    val payers: Map<String, Double> = emptyMap(),
    val debtors: Map<String, Double> = emptyMap(),
    val expenseType: ExpenseType = ExpenseType.NORMAL,
    val eventId: String = "",
    val frequency: Frequency = Frequency.ONCE,
    val startDate: Long = Clock.System.now().toEpochMilliseconds(),
): IExpense{

    fun calculateDebtsAndPayers(): Pair<Map<String, Double>, Map<String, Double>> {
        return when (splitMethod) {
            SplitMethod.EQUALLY -> calculateEqually()
            SplitMethod.PERCENTAGES -> calculateByPercentages()
            SplitMethod.CUSTOM -> calculateCustom()
        }
    }

    private fun calculateEqually(): Pair<Map<String, Double>, Map<String, Double>> {
        val updatedPayers = payers
        val updatedDebtors = debtors
        return Pair(updatedPayers, updatedDebtors)
    }

    private fun calculateByPercentages(): Pair<Map<String, Double>, Map<String, Double>> {
        val updatedPayers = payers.mapValues { (_,percentage) -> amount * (percentage / 100) }
        val updatedDebtors = debtors.mapValues { (_, percentage) -> amount * (percentage / 100) }
        return Pair(updatedPayers, updatedDebtors)
    }

    private fun calculateCustom(): Pair<Map<String, Double>, Map<String, Double>> {
        val updatedPayers = payers
        val updatedDebtors = debtors
        return Pair(updatedPayers, updatedDebtors)
    }

}