package com.ragl.divide.domain.services

import com.ragl.divide.data.models.Event
import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.data.models.EventPayment

interface GroupExpenseService {
    /**
     * Calcula las deudas basadas en gastos y pagos
     */
    fun calculateDebts(
        expenses: Collection<EventExpense>,
        payments: Collection<EventPayment>,
        simplify: Boolean = false,
        expensesToSettle: MutableList<String>? = null,
        paymentsToSettle: MutableList<String>? = null
    ): Map<String, Map<String, Double>>
    
    /**
     * Consolida las deudas de múltiples eventos
     */
    fun consolidateDebtsFromEvents(events: Collection<Event>): Map<String, Map<String, Double>>
    
    /**
     * Función utilitaria para consolidar deudas de eventos desde un mapa
     */
    fun consolidateDebtsFromEventsMap(eventsMap: Map<String, Event>): Map<String, Map<String, Double>>
    
    /**
     * Obtiene un mapa con el ID del evento y sus currentDebts
     */
    fun getEventDebtsMap(eventsMap: Map<String, Event>): Map<String, Map<String, Map<String, Double>>>
} 