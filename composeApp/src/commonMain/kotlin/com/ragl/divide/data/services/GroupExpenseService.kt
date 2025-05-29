package com.ragl.divide.data.services

import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.ui.utils.toTwoDecimals

class GroupExpenseService {
    fun calculateDebts(
        expenses: Collection<GroupExpense>,
        payments: Collection<Payment>,
        simplify: Boolean = false,
        expensesToSettle: MutableList<String>? = null,
        paymentsToSettle: MutableList<String>? = null
    ): Map<String, Map<String, Double>> {
        val balances = mutableMapOf<String, MutableMap<String, Double>>()

        // Filtrar gastos eliminados o ya saldados
        val activeExpenses = expenses.filter { 
            !it.deleted && !it.settled 
        }
        
        // Guardar IDs de gastos activos para posible liquidación automática
        val activeExpenseIds = activeExpenses.map { it.id }

        for (expense in activeExpenses) {
            val (updatedPayers, updatedDebtors) = expense.calculateDebtsAndPayers()

            for ((debtor, debtAmount) in updatedDebtors) {
                for ((payer, _) in updatedPayers) {
                    if (debtor == payer) continue

                    balances[debtor] = balances.getOrElse(debtor) { mutableMapOf() }.apply {
                        this[payer] = (this[payer] ?: 0.0) + debtAmount
                    }
                }
            }
        }

        // Filtrar pagos ya saldados
        val activePayments = payments.filter { !it.settled }
        
        // Guardar IDs de pagos activos para posible liquidación automática
        val activePaymentIds = activePayments.map { it.id }

        for (payment in activePayments) {
            // Verificar si ya existe una deuda de 'to' hacia 'from'
            val toUserMap = balances[payment.to]
            val existingDebtToFrom = toUserMap?.get(payment.from) ?: 0.0
            
            if (existingDebtToFrom > 0) {
                // El destinatario (to) ya debe dinero al remitente (from)
                // Incrementar esta deuda según el requisito
                balances[payment.to]!![payment.from] = existingDebtToFrom + payment.amount
            } else {
                // No existe una deuda de 'to' a 'from', verificar si 'from' debe a 'to'
                val fromOwesToAmount = balances.getOrElse(payment.from) { mutableMapOf() }[payment.to] ?: 0.0
                
                if (fromOwesToAmount > 0) {
                    // 'from' le debe dinero a 'to', reducir esa deuda
                    val newDebt = (fromOwesToAmount - payment.amount).coerceAtLeast(0.0)
                    
                    if (newDebt > 0.01) {
                        // Todavía queda deuda
                        balances[payment.from]!![payment.to] = newDebt
                    } else {
                        // La deuda está pagada, eliminar la entrada
                        balances[payment.from]!!.remove(payment.to)
                        // Si no hay más deudas para este usuario, eliminar el mapa
                        if (balances[payment.from]!!.isEmpty()) {
                            balances.remove(payment.from)
                        }
                        
                        // Si el pago excede la deuda, crear una deuda en dirección contraria
                        val excessAmount = payment.amount - fromOwesToAmount
                        if (excessAmount > 0.01) {
                            balances[payment.to] = balances.getOrElse(payment.to) { mutableMapOf() }.apply {
                                this[payment.from] = (this[payment.from] ?: 0.0) + excessAmount
                            }
                        }
                    }
                } else {
                    // No hay deuda en ninguna dirección, crear una donde 'to' debe a 'from'
                    balances[payment.to] = balances.getOrElse(payment.to) { mutableMapOf() }.apply {
                        this[payment.from] = (this[payment.from] ?: 0.0) + payment.amount
                    }
                }
            }
        }

        val debts = balances.mapValues { (_, map) ->
            map.mapValues { (_, value) -> value.toTwoDecimals() }
                .filterValues { kotlin.math.abs(it) > 0.01 }
        }.filterValues { it.isNotEmpty() }

        // Si no hay deudas pendientes y hay gastos/pagos para liquidar
        if (debts.isEmpty() && (activeExpenseIds.isNotEmpty() || activePaymentIds.isNotEmpty())) {
            // Agregar todos los gastos y pagos activos a las listas de liquidación
            expensesToSettle?.addAll(activeExpenseIds)
            paymentsToSettle?.addAll(activePaymentIds)
            return emptyMap()
        }

        val result = if (simplify) {
            val simplified = simplifyDebts(debts)
            // Si después de simplificar las deudas el resultado es vacío, también marcar como saldados
            if (simplified.isEmpty() && (activeExpenseIds.isNotEmpty() || activePaymentIds.isNotEmpty())) {
                expensesToSettle?.addAll(activeExpenseIds)
                paymentsToSettle?.addAll(activePaymentIds)
            }
            simplified
        } else {
            debts
        }
        
        return result
    }

    private fun simplifyDebts(
        debts: Map<String, Map<String, Double>>
    ): Map<String, Map<String, Double>> {
        if (debts.isEmpty()) return emptyMap()
        
        // Paso 1: Calcular las deudas netas entre pares de personas
        val netDebts = mutableMapOf<Pair<String, String>, Double>()

        for ((debtor, creditors) in debts) {
            for ((creditor, amount) in creditors) {
                val key = if (debtor < creditor) Pair(debtor, creditor) else Pair(creditor, debtor)
                val signedAmount = if (debtor < creditor) amount else -amount

                netDebts[key] = (netDebts[key] ?: 0.0) + signedAmount
            }
        }

        // Paso 2: Crear un mapa inicial de deudas simplificadas
        val simplified = mutableMapOf<String, MutableMap<String, Double>>()

        for ((pair, amount) in netDebts) {
            // Si la deuda neta es insignificante, ignorarla
            if (kotlin.math.abs(amount) < 0.01) continue

            val (from, to, amt) = if (amount > 0) {
                Triple(pair.first, pair.second, amount)
            } else {
                Triple(pair.second, pair.first, -amount)
            }

            simplified.getOrPut(from) { mutableMapOf() }[to] = amt
        }
        
        // Paso 3: Aplicar el algoritmo de transferencia de deudas
        var wasSimplified: Boolean
        do {
            wasSimplified = false
            
            // Optimización: Buscar caminos directos para simplificar deudas circulares
            val debtors = simplified.keys.toList()
            
            for (debtor in debtors) {
                val creditors = simplified[debtor]?.keys?.toList() ?: continue
                
                for (creditor in creditors) {
                    // Caso especial: detectar deudas circulares directas (A debe a B y B debe a A)
                    val mutualDebt = simplified[creditor]?.get(debtor)
                    if (mutualDebt != null) {
                        val debtAmount = simplified[debtor]!![creditor] ?: 0.0
                        
                        // Cancelar la menor contra la mayor
                        val minDebt = kotlin.math.min(debtAmount, mutualDebt)
                        val remainingDebtorDebt = (debtAmount - minDebt).coerceAtLeast(0.0)
                        val remainingCreditorDebt = (mutualDebt - minDebt).coerceAtLeast(0.0)
                        
                        var changed = false
                        
                        // Actualizar o eliminar la deuda del deudor
                        if (remainingDebtorDebt > 0.01) {
                            simplified[debtor]!![creditor] = remainingDebtorDebt
                        } else {
                            simplified[debtor]!!.remove(creditor)
                            if (simplified[debtor]!!.isEmpty()) {
                                simplified.remove(debtor)
                            }
                            changed = true
                        }
                        
                        // Actualizar o eliminar la deuda del acreedor
                        if (remainingCreditorDebt > 0.01) {
                            simplified[creditor]!![debtor] = remainingCreditorDebt
                        } else {
                            simplified[creditor]!!.remove(debtor)
                            if (simplified[creditor]!!.isEmpty()) {
                                simplified.remove(creditor)
                            }
                            changed = true
                        }
                        
                        if (changed) {
                            wasSimplified = true
                            break  // Salir del bucle interno
                        }
                        
                        continue  // Pasar a la siguiente combinación
                    }
                    
                    // Si A debe a B y B debe a C, entonces A podría deber directamente a C
                    val secondaryCreditors = simplified[creditor]?.keys?.toList() ?: continue
                    
                    for (secondaryCreditor in secondaryCreditors) {
                        // Evitar ciclos
                        if (secondaryCreditor == debtor) continue
                        
                        // Calculamos el monto que se puede transferir (el mínimo entre las dos deudas)
                        val debtToCreditor = simplified[debtor]?.get(creditor) ?: 0.0
                        val creditorToSecondary = simplified[creditor]?.get(secondaryCreditor) ?: 0.0
                        val transferAmount = kotlin.math.min(debtToCreditor, creditorToSecondary)
                        
                        if (transferAmount > 0.01) {
                            // Transferir la deuda
                            // 1. Reducir la deuda original de debtor a creditor
                            val remainingDebt = debtToCreditor - transferAmount
                            if (remainingDebt > 0.01) {
                                simplified[debtor]!![creditor] = remainingDebt
                            } else {
                                simplified[debtor]!!.remove(creditor)
                                if (simplified[debtor]!!.isEmpty()) {
                                    simplified.remove(debtor)
                                }
                            }
                            
                            // 2. Reducir la deuda de creditor a secondaryCreditor
                            val remainingSecondaryDebt = creditorToSecondary - transferAmount
                            if (remainingSecondaryDebt > 0.01) {
                                simplified[creditor]!![secondaryCreditor] = remainingSecondaryDebt
                            } else {
                                simplified[creditor]!!.remove(secondaryCreditor)
                                if (simplified[creditor]!!.isEmpty()) {
                                    simplified.remove(creditor)
                                }
                            }
                            
                            // 3. Verificar si ya existe una deuda en dirección contraria antes de crear una nueva
                            val existingReverseDebt = simplified[secondaryCreditor]?.get(debtor) ?: 0.0
                            if (existingReverseDebt > 0.0) {
                                // Si existe deuda en dirección contraria, cancelarlas entre sí
                                val diff = transferAmount - existingReverseDebt
                                if (diff > 0.01) {
                                    // debtor sigue debiendo a secondaryCreditor
                                    simplified.getOrPut(debtor) { mutableMapOf() }[secondaryCreditor] = diff
                                    // Eliminar la deuda inversa
                                    simplified[secondaryCreditor]!!.remove(debtor)
                                    if (simplified[secondaryCreditor]!!.isEmpty()) {
                                        simplified.remove(secondaryCreditor)
                                    }
                                } else if (diff < -0.01) {
                                    // secondaryCreditor sigue debiendo a debtor, pero menos
                                    simplified[secondaryCreditor]!![debtor] = -diff
                                } else {
                                    // Las deudas se cancelan exactamente
                                    simplified[secondaryCreditor]!!.remove(debtor)
                                    if (simplified[secondaryCreditor]!!.isEmpty()) {
                                        simplified.remove(secondaryCreditor)
                                    }
                                }
                            } else {
                                // No hay deuda inversa, crear nueva deuda
                                simplified.getOrPut(debtor) { mutableMapOf() }[secondaryCreditor] = 
                                    (simplified[debtor]?.get(secondaryCreditor) ?: 0.0) + transferAmount
                            }
                            
                            wasSimplified = true
                            break  // Salir del bucle interno ya que hemos modificado las colecciones
                        }
                    }
                    
                    if (wasSimplified) break  // Salir del bucle externo si ya hicimos una simplificación
                }
                
                if (wasSimplified) break  // Salir del bucle principal si ya hicimos una simplificación
            }
        } while (wasSimplified)
        
        // Paso 4: Redondear a dos decimales
        return simplified.mapValues { (_, creditors) ->
            creditors.mapValues { (_, amount) -> amount.toTwoDecimals() }
                .filterValues { it > 0.01 }
                .toMutableMap()
        }.filterValues { it.isNotEmpty() }
    }
}