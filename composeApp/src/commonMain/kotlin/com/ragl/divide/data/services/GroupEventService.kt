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
        
        // Utiliza el servicio existente de cálculo de deudas
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
     * Liquida un evento, marcando todos sus gastos y pagos como saldados
     */
    fun settleEvent(event: GroupEvent): GroupEvent {
        // Marcar todos los gastos como liquidados
        val settledExpenses = event.expenses.mapValues { (_, expense) ->
            expense.copy(settled = true)
        }
        
        // Marcar todos los pagos como liquidados
        val settledPayments = event.payments.mapValues { (_, payment) ->
            payment.copy(settled = true)
        }
        
        // Devolver el evento actualizado
        return event.copy(
            expenses = settledExpenses,
            payments = settledPayments,
            settled = true,
            currentDebts = emptyMap() // Al liquidar, no hay deudas pendientes
        )
    }
    
    /**
     * Reabre un evento previamente liquidado
     */
    fun reopenEvent(event: GroupEvent): GroupEvent {
        // Desmarcar todos los gastos como liquidados
        val unsettledExpenses = event.expenses.mapValues { (_, expense) ->
            expense.copy(settled = false)
        }
        
        // Desmarcar todos los pagos como liquidados
        val unsettledPayments = event.payments.mapValues { (_, payment) ->
            payment.copy(settled = false)
        }
        
        // Recalcular deudas
        val newDebts = calculateEventDebts(
            event.copy(
                expenses = unsettledExpenses,
                payments = unsettledPayments
            )
        )
        
        // Devolver el evento actualizado
        return event.copy(
            expenses = unsettledExpenses,
            payments = unsettledPayments,
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
        
        // Recalcular deudas con el nuevo pago
        val updatedEvent = event.copy(payments = updatedPayments)
        val newDebts = calculateEventDebts(updatedEvent)
        
        return updatedEvent.copy(currentDebts = newDebts)
    }
} 