package com.ragl.divide.data.services

import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.GroupUser
import com.ragl.divide.data.models.SplitMethod
import com.ragl.divide.ui.utils.toTwoDecimals

// GroupExpenseService.kt
class GroupExpenseService {
    /**
     * Calcula la deuda según el méthod de división del gasto
     */
    private fun calculateDebt(expense: GroupExpense, amount: Double): Double =
        if (expense.splitMethod == SplitMethod.PERCENTAGES) (amount * expense.amount) / 100 else amount

    /**
     * Calcula las actualizaciones de balance para los pagadores de un gasto
     */
    fun calculatePayerBalanceUpdates(
        expense: GroupExpense,
        currentUsers: Map<String, GroupUser>
    ): Map<String, GroupUser> {
        val updatedUsers = currentUsers.toMutableMap()

        expense.paidBy.entries.forEach { (payerId, amountPaid) ->
            val groupUser = updatedUsers[payerId] ?: return@forEach
            val newOwedMap = groupUser.owed.toMutableMap()

            expense.debtors.entries.forEach { (debtorId, debtorAmount) ->
                val debt = calculateDebt(expense, debtorAmount)
                newOwedMap[debtorId] = ((newOwedMap[debtorId] ?: 0.0) + debt).toTwoDecimals()
            }

            val payerOwed = expense.amount - calculateDebt(expense, amountPaid)
            updatedUsers[payerId] = groupUser.copy(
                owed = newOwedMap,
                totalOwed = (groupUser.totalOwed + payerOwed).toTwoDecimals()
            )
        }

        return updatedUsers
    }

    /**
     * Calcula las actualizaciones de balance para los deudores de un gasto
     */
    fun calculateDebtorBalanceUpdates(
        expense: GroupExpense,
        currentUsers: Map<String, GroupUser>
    ): Map<String, GroupUser> {
        val updatedUsers = currentUsers.toMutableMap()

        expense.debtors.entries.forEach { (debtorId, amount) ->
            val groupUser = updatedUsers[debtorId] ?: return@forEach
            val newDebtsMap = groupUser.debts.toMutableMap()
            val totalDebt = calculateDebt(expense, amount)

            expense.paidBy.keys.forEach { payerId ->
                newDebtsMap[payerId] = ((newDebtsMap[payerId] ?: 0.0) + totalDebt).toTwoDecimals()
            }

            updatedUsers[debtorId] = groupUser.copy(
                debts = newDebtsMap,
                totalDebt = (groupUser.totalDebt + totalDebt).toTwoDecimals()
            )
        }

        return updatedUsers
    }

    /**
     * Calcula las actualizaciones de balance cuando se actualiza un gasto
     */
    fun calculateUpdateExpenseBalanceChanges(
        oldExpense: GroupExpense,
        newExpense: GroupExpense,
        currentUsers: Map<String, GroupUser>
    ): Map<String, GroupUser> {
        val updatedUsers = currentUsers.toMutableMap()

        // Actualizar pagadores
        newExpense.paidBy.entries.forEach { (payerId, amountPaid) ->
            val groupUser = updatedUsers[payerId] ?: return@forEach
            val newOwedMap = groupUser.owed.toMutableMap()

            // Restar deudas viejas
            oldExpense.debtors.entries.forEach { (debtorId, oldDebtorAmount) ->
                val oldDebt = calculateDebt(oldExpense, oldDebtorAmount)
                newOwedMap[debtorId] = ((newOwedMap[debtorId] ?: 0.0) - oldDebt).toTwoDecimals()
            }

            // Sumar deudas nuevas
            newExpense.debtors.entries.forEach { (debtorId, newDebtorAmount) ->
                val newDebt = calculateDebt(newExpense, newDebtorAmount)
                newOwedMap[debtorId] = ((newOwedMap[debtorId] ?: 0.0) + newDebt).toTwoDecimals()
            }

            val newPayerOwed = newExpense.amount - calculateDebt(newExpense, amountPaid)
            val oldPayerOwed =
                oldExpense.amount - calculateDebt(oldExpense, oldExpense.paidBy[payerId] ?: 0.0)

            updatedUsers[payerId] = groupUser.copy(
                owed = newOwedMap,
                totalOwed = (groupUser.totalOwed - oldPayerOwed + newPayerOwed).toTwoDecimals()
            )
        }

        // Actualizar deudores
        newExpense.debtors.entries.forEach { (debtorId, amount) ->
            val groupUser = updatedUsers[debtorId] ?: return@forEach
            val newDebtsMap = groupUser.debts.toMutableMap()

            // Restar deudas viejas
            oldExpense.paidBy.entries.forEach { (payerId, payerAmount) ->
                val oldDebt = calculateDebt(oldExpense, payerAmount)
                newDebtsMap[payerId] = ((newDebtsMap[payerId] ?: 0.0) - oldDebt).toTwoDecimals()
            }

            // Sumar deudas nuevas
            newExpense.paidBy.entries.forEach { (payerId, payerAmount) ->
                val newDebt = calculateDebt(newExpense, payerAmount)
                newDebtsMap[payerId] = ((newDebtsMap[payerId] ?: 0.0) + newDebt).toTwoDecimals()
            }

            updatedUsers[debtorId] = groupUser.copy(
                debts = newDebtsMap,
                totalDebt = (groupUser.totalDebt - (oldExpense.debtors[debtorId]
                    ?: 0.0) + amount).toTwoDecimals()
            )
        }

        return updatedUsers
    }

    /**
     * Calcula las actualizaciones de balance cuando se elimina un gasto
     */
    fun calculateDeleteExpenseBalanceChanges(
        expense: GroupExpense,
        currentUsers: Map<String, GroupUser>
    ): Map<String, GroupUser> {
        val updatedUsers = currentUsers.toMutableMap()

        // Actualizar pagadores
        expense.paidBy.entries.forEach { (userId, amountPaid) ->
            val groupUser = updatedUsers[userId] ?: return@forEach
            val newOwedMap = groupUser.owed.toMutableMap()

            expense.debtors.entries.forEach { (debtorId, debtorAmount) ->
                val debt = calculateDebt(expense, debtorAmount)
                newOwedMap[debtorId] =
                    if (newOwedMap[debtorId] == null) 0.0 else (newOwedMap[debtorId]!! - debt).toTwoDecimals()
            }

            val payerOwed = expense.amount - calculateDebt(expense, amountPaid)
            updatedUsers[userId] = groupUser.copy(
                totalOwed = (groupUser.totalOwed - payerOwed).toTwoDecimals(),
                owed = newOwedMap
            )
        }

        // Actualizar deudores
        expense.debtors.entries.forEach { (userId, amount) ->
            val groupUser = updatedUsers[userId] ?: return@forEach
            val newDebtsMap = groupUser.debts.toMutableMap()
            val totalDebt = calculateDebt(expense, amount)

            expense.paidBy.keys.forEach { payerId ->
                newDebtsMap[payerId] =
                    if (newDebtsMap[payerId] == null) 0.0 else (newDebtsMap[payerId]!! - totalDebt).toTwoDecimals()
            }

            updatedUsers[userId] = groupUser.copy(
                totalDebt = (groupUser.totalDebt - totalDebt).toTwoDecimals(),
                debts = newDebtsMap
            )
        }

        return updatedUsers
    }
}