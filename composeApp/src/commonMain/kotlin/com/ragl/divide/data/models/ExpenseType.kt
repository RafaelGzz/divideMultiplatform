package com.ragl.divide.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class ExpenseType {
    NORMAL,       // Gasto normal dentro del grupo
    RECURRING,    // Gasto recurrente fuera de eventos
    EVENT_BASED   // Gasto asociado a un evento específico
} 