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
    override val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    override val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
    val splitMethod: SplitMethod = SplitMethod.EQUALLY,
    val payers: Map<String, Double> = emptyMap(),
    val debtors: Map<String, Double> = emptyMap(),
    val activityLog: Map<String, ActivityLog> = emptyMap(),
    val deleted: Boolean = false,
    val expenseType: ExpenseType = ExpenseType.NORMAL,
    val eventId: String = "", // ID del evento al que pertenece (solo si expenseType es EVENT_BASED)
    val frequency: Frequency = Frequency.ONCE, // Solo relevante si expenseType es RECURRING
    val startDate: Long = Clock.System.now().toEpochMilliseconds(), // Para gastos recurrentes
): IExpense{

    fun calculateDebtsAndPayers(): Pair<Map<String, Double>, Map<String, Double>> {
        return when (splitMethod) {
            SplitMethod.EQUALLY -> calculateEqually()
            SplitMethod.PERCENTAGES -> calculateByPercentages()
            SplitMethod.CUSTOM -> calculateCustom()
        }
    }

    private fun calculateEqually(): Pair<Map<String, Double>, Map<String, Double>> {
//        val amountPerUser = amount / payers.size
//        val updatedPayers = payers.mapValues { amountPerUser }
//        val updatedDebtors = debtors.mapValues { amountPerUser }
//        return Pair(updatedPayers, updatedDebtors)

        val updatedPayers = payers // Already contains the correct amount
        val updatedDebtors = debtors // Already contains the correct amount
        return Pair(updatedPayers, updatedDebtors)
    }

    private fun calculateByPercentages(): Pair<Map<String, Double>, Map<String, Double>> {
//        val totalPercentage = payers.values.sum()
//        if (totalPercentage != 100.0) throw IllegalArgumentException("Percentages must sum to 100%")


        val updatedPayers = payers.mapValues { (_,percentage) -> amount * (percentage / 100) }
        val updatedDebtors = debtors.mapValues { (_, percentage) -> amount * (percentage / 100) }
        return Pair(updatedPayers, updatedDebtors)
    }

    private fun calculateCustom(): Pair<Map<String, Double>, Map<String, Double>> {
//        val totalAmount = payers.values.sum()
//        if (totalAmount != amount) throw IllegalArgumentException("Custom amounts must sum to the total amount")

        val updatedPayers = payers // Already contains the correct amount
        val updatedDebtors = debtors // Already contains the correct amount
        return Pair(updatedPayers, updatedDebtors)
    }

}