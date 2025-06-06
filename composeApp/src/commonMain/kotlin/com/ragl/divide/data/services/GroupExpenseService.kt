package com.ragl.divide.data.services

import com.ragl.divide.data.models.GroupEvent
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.ui.utils.toTwoDecimals

class GroupExpenseService {
    
    companion object {
        private const val MINIMUM_DEBT = 0.01
    }
    
    fun calculateDebts(
        expenses: Collection<GroupExpense>,
        payments: Collection<Payment>,
        simplify: Boolean = false,
        expensesToSettle: MutableList<String>? = null,
        paymentsToSettle: MutableList<String>? = null
    ): Map<String, Map<String, Double>> {
        val balances = mutableMapOf<String, MutableMap<String, Double>>()
        
        // Procesar gastos activos (solo filtrar por deleted)
        val activeExpenses = expenses.filter { !it.deleted }
        processExpenses(activeExpenses, balances)
        
        // Procesar todos los pagos
        processPayments(payments, balances, simplify)
        
        // Limpiar y formatear deudas
        val debts = cleanAndFormatDebts(balances)
        
        // Manejar liquidación automática si no hay deudas
        if (debts.isEmpty()) {
            expensesToSettle?.addAll(activeExpenses.map { it.id })
            paymentsToSettle?.addAll(payments.map { it.id })
            return emptyMap()
        }
        
        // Aplicar simplificación si es necesario
        return if (simplify) {
            val simplified = simplifyDebts(debts)
            if (simplified.isEmpty()) {
                expensesToSettle?.addAll(activeExpenses.map { it.id })
                paymentsToSettle?.addAll(payments.map { it.id })
            }
            simplified
        } else {
            debts
        }
    }
    
    private fun processExpenses(
        expenses: Collection<GroupExpense>,
        balances: MutableMap<String, MutableMap<String, Double>>
    ) {
        expenses.forEach { expense ->
            val (payers, debtors) = expense.calculateDebtsAndPayers()
            debtors.forEach { (debtor, debtAmount) ->
                payers.forEach { (payer, _) ->
                    if (debtor != payer) {
                        addDebt(balances, debtor, payer, debtAmount)
                    }
                }
            }
        }
    }
    
    private fun processPayments(
        payments: Collection<Payment>,
        balances: MutableMap<String, MutableMap<String, Double>>,
        simplify: Boolean
    ) {
        payments.forEach { payment ->
            if (simplify) {
                processPaymentWithSimplification(payment, balances)
            } else {
                addDebt(balances, payment.to, payment.from, payment.amount)
            }
        }
    }
    
    private fun processPaymentWithSimplification(
        payment: Payment,
        balances: MutableMap<String, MutableMap<String, Double>>
    ) {
        val existingDebtToFrom = balances[payment.to]?.get(payment.from) ?: 0.0
        
        if (existingDebtToFrom > 0) {
            // Incrementar deuda existente
            balances[payment.to]!![payment.from] = existingDebtToFrom + payment.amount
        } else {
            val fromOwesToAmount = balances[payment.from]?.get(payment.to) ?: 0.0
            
            if (fromOwesToAmount > 0) {
                // Reducir deuda existente
                val newDebt = (fromOwesToAmount - payment.amount).coerceAtLeast(0.0)
                
                if (newDebt > MINIMUM_DEBT) {
                    balances[payment.from]!![payment.to] = newDebt
                } else {
                    removeDebt(balances, payment.from, payment.to)
                    
                    // Manejar exceso
                    val excessAmount = payment.amount - fromOwesToAmount
                    if (excessAmount > MINIMUM_DEBT) {
                        addDebt(balances, payment.to, payment.from, excessAmount)
                    }
                }
            } else {
                // Crear nueva deuda
                addDebt(balances, payment.to, payment.from, payment.amount)
            }
        }
    }
    
    private fun addDebt(
        balances: MutableMap<String, MutableMap<String, Double>>,
        debtor: String,
        creditor: String,
        amount: Double
    ) {
        balances.getOrPut(debtor) { mutableMapOf() }[creditor] = 
            (balances[debtor]?.get(creditor) ?: 0.0) + amount
    }
    
    private fun removeDebt(
        balances: MutableMap<String, MutableMap<String, Double>>,
        debtor: String,
        creditor: String
    ) {
        balances[debtor]?.remove(creditor)
        if (balances[debtor]?.isEmpty() == true) {
            balances.remove(debtor)
        }
    }
    
    private fun cleanAndFormatDebts(
        balances: MutableMap<String, MutableMap<String, Double>>
    ): Map<String, Map<String, Double>> {
        return balances.mapValues { (_, map) ->
            map.mapValues { (_, value) -> value.toTwoDecimals() }
                .filterValues { kotlin.math.abs(it) > MINIMUM_DEBT }
        }.filterValues { it.isNotEmpty() }
    }
    
    private fun simplifyDebts(
        debts: Map<String, Map<String, Double>>
    ): Map<String, Map<String, Double>> {
        if (debts.isEmpty()) return emptyMap()
        
        // Calcular deudas netas entre pares
        val netDebts = calculateNetDebts(debts)
        
        // Crear mapa inicial simplificado
        val simplified = createInitialSimplifiedMap(netDebts)
        
        // Aplicar simplificación iterativa
        applyIterativeSimplification(simplified)
        
        return cleanAndFormatDebts(simplified)
    }
    
    private fun calculateNetDebts(
        debts: Map<String, Map<String, Double>>
    ): Map<Pair<String, String>, Double> {
        val netDebts = mutableMapOf<Pair<String, String>, Double>()
        
        debts.forEach { (debtor, creditors) ->
            creditors.forEach { (creditor, amount) ->
                val key = if (debtor < creditor) Pair(debtor, creditor) else Pair(creditor, debtor)
                val signedAmount = if (debtor < creditor) amount else -amount
                netDebts[key] = (netDebts[key] ?: 0.0) + signedAmount
            }
        }
        
        return netDebts
    }
    
    private fun createInitialSimplifiedMap(
        netDebts: Map<Pair<String, String>, Double>
    ): MutableMap<String, MutableMap<String, Double>> {
        val simplified = mutableMapOf<String, MutableMap<String, Double>>()
        
        netDebts.forEach { (pair, amount) ->
            if (kotlin.math.abs(amount) >= MINIMUM_DEBT) {
                val (from, to, amt) = if (amount > 0) {
                    Triple(pair.first, pair.second, amount)
                } else {
                    Triple(pair.second, pair.first, -amount)
                }
                simplified.getOrPut(from) { mutableMapOf() }[to] = amt
            }
        }
        
        return simplified
    }
    
    private fun applyIterativeSimplification(
        simplified: MutableMap<String, MutableMap<String, Double>>
    ) {
        var wasSimplified: Boolean
        do {
            wasSimplified = false
            
            // Buscar y resolver deudas circulares directas
            wasSimplified = resolveMutualDebts(simplified) || wasSimplified
            
            // Buscar y resolver cadenas de deudas
            wasSimplified = resolveDebtChains(simplified) || wasSimplified
            
        } while (wasSimplified)
    }
    
    private fun resolveMutualDebts(
        simplified: MutableMap<String, MutableMap<String, Double>>
    ): Boolean {
        val debtors = simplified.keys.toList()
        
        for (debtor in debtors) {
            val creditors = simplified[debtor]?.keys?.toList() ?: continue
            
            for (creditor in creditors) {
                val mutualDebt = simplified[creditor]?.get(debtor)
                if (mutualDebt != null) {
                    val debtAmount = simplified[debtor]!![creditor] ?: 0.0
                    val minDebt = kotlin.math.min(debtAmount, mutualDebt)
                    
                    // Reducir ambas deudas
                    updateOrRemoveDebt(simplified, debtor, creditor, debtAmount - minDebt)
                    updateOrRemoveDebt(simplified, creditor, debtor, mutualDebt - minDebt)
                    
                    return true
                }
            }
        }
        return false
    }
    
    private fun resolveDebtChains(
        simplified: MutableMap<String, MutableMap<String, Double>>
    ): Boolean {
        val debtors = simplified.keys.toList()
        
        for (debtor in debtors) {
            val creditors = simplified[debtor]?.keys?.toList() ?: continue
            
            for (creditor in creditors) {
                val secondaryCreditors = simplified[creditor]?.keys?.toList() ?: continue
                
                for (secondaryCreditor in secondaryCreditors) {
                    if (secondaryCreditor == debtor) continue
                    
                    val debtToCreditor = simplified[debtor]?.get(creditor) ?: 0.0
                    val creditorToSecondary = simplified[creditor]?.get(secondaryCreditor) ?: 0.0
                    val transferAmount = kotlin.math.min(debtToCreditor, creditorToSecondary)
                    
                    if (transferAmount > MINIMUM_DEBT) {
                        // Transferir deuda
                        updateOrRemoveDebt(simplified, debtor, creditor, debtToCreditor - transferAmount)
                        updateOrRemoveDebt(simplified, creditor, secondaryCreditor, creditorToSecondary - transferAmount)
                        
                        // Manejar deuda resultante
                        val existingReverseDebt = simplified[secondaryCreditor]?.get(debtor) ?: 0.0
                        if (existingReverseDebt > 0.0) {
                            val diff = transferAmount - existingReverseDebt
                            updateOrRemoveDebt(simplified, secondaryCreditor, debtor, 0.0)
                            if (kotlin.math.abs(diff) > MINIMUM_DEBT) {
                                if (diff > 0) {
                                    addDebt(simplified, debtor, secondaryCreditor, diff)
                                } else {
                                    addDebt(simplified, secondaryCreditor, debtor, -diff)
                                }
                            }
                        } else {
                            addDebt(simplified, debtor, secondaryCreditor, transferAmount)
                        }
                        
                        return true
                    }
                }
            }
        }
        return false
    }
    
    private fun updateOrRemoveDebt(
        simplified: MutableMap<String, MutableMap<String, Double>>,
        debtor: String,
        creditor: String,
        newAmount: Double
    ) {
        if (newAmount > MINIMUM_DEBT) {
            simplified[debtor]!![creditor] = newAmount
        } else {
            removeDebt(simplified, debtor, creditor)
        }
    }

    /**
     * Consolida las deudas de múltiples eventos.
     */
    fun consolidateDebtsFromEvents(events: Collection<GroupEvent>): Map<String, Map<String, Double>> {
        val consolidatedBalances = mutableMapOf<String, MutableMap<String, Double>>()

        events.filter { !it.settled }.forEach { event ->
            event.currentDebts.forEach { (debtor, debts) ->
                debts.forEach { (creditor, amount) ->
                    if (amount > MINIMUM_DEBT) {
                        addDebt(consolidatedBalances, debtor, creditor, amount)
                    }
                }
            }
        }

        return cleanAndFormatDebts(consolidatedBalances)
    }

    /**
     * Función utilitaria para consolidar deudas de eventos desde un mapa.
     */
    fun consolidateDebtsFromEventsMap(eventsMap: Map<String, GroupEvent>): Map<String, Map<String, Double>> {
        return consolidateDebtsFromEvents(eventsMap.values)
    }

    /**
     * Obtiene un mapa con el ID del evento y sus currentDebts.
     */
    fun getEventDebtsMap(eventsMap: Map<String, GroupEvent>): Map<String, Map<String, Map<String, Double>>> {
        return eventsMap.mapValues { (_, event) -> event.currentDebts }
    }
}