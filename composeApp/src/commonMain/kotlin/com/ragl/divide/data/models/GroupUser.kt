package com.ragl.divide.data.models

data class GroupUser(
    val id: String = "",
    val debts: Map<String, Double> = emptyMap(),
    val owed: Map<String, Double> = emptyMap(),
    val totalOwed: Double = 0.0,
    val totalDebt: Double = 0.0,
    val netDebt: Double = 0.0
)