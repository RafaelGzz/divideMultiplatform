package com.ragl.divide.data.services

import com.ragl.divide.data.models.GroupEvent
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.Payment

class GroupEventService(private val groupExpenseService: GroupExpenseService) {
    
    /**
     * Calcula las deudas dentro de un evento específico
     */
    fun calculateEventDebts(
        event: GroupEvent,
        simplify: Boolean = false
    ): Map<String, Map<String, Double>> {
        val expensesToSettle = mutableListOf<String>()
        val paymentsToSettle = mutableListOf<String>()
        
        val debts = groupExpenseService.calculateDebts(
            event.expenses.values,
            event.payments.values,
            simplify,
            expensesToSettle,
            paymentsToSettle
        )
        
        return debts
    }
    
    /**
     * Determina si un evento puede ser liquidado (todas las deudas son 0)
     */
    fun canSettleEvent(event: GroupEvent, simplify: Boolean = false): Boolean {
        val debts = calculateEventDebts(event, simplify)
        return debts.isEmpty()
    }
    
    /**
     * Liquida un evento, marcando el evento como saldado
     */
    fun settleEvent(event: GroupEvent): GroupEvent {
        return event.copy(
            settled = true
        )
    }
    
    /**
     * Reabre un evento previamente liquidado
     */
    fun reopenEvent(event: GroupEvent): GroupEvent {
        val newDebts = calculateEventDebts(event)
        
        return event.copy(
            settled = false,
            currentDebts = newDebts
        )
    }
    
    /**
     * Agrega un gasto a un evento
     */
    fun addExpenseToEvent(event: GroupEvent, expense: GroupExpense): GroupEvent {
        val updatedExpense = expense.copy(
            eventId = event.id,
            expenseType = com.ragl.divide.data.models.ExpenseType.EVENT_BASED
        )
        
        val updatedExpenses = event.expenses + (updatedExpense.id to updatedExpense)
        
        // Recalcular deudas con el nuevo gasto
        val updatedEvent = event.copy(expenses = updatedExpenses)
        val newDebts = calculateEventDebts(updatedEvent)
        
        return updatedEvent.copy(currentDebts = newDebts)
    }
    
    /**
     * Agrega un pago a un evento
     */
    fun addPaymentToEvent(event: GroupEvent, payment: Payment): GroupEvent {
        val updatedPayment = payment.copy(eventId = event.id)
        
        val updatedPayments = event.payments + (updatedPayment.id to updatedPayment)
        
        val updatedEvent = event.copy(payments = updatedPayments)
        val newDebts = calculateEventDebts(updatedEvent)
        
        return updatedEvent.copy(currentDebts = newDebts)
    }
} 