package com.ragl.divide.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val expenses: Map<String, GroupExpense> = emptyMap(),
    val payments: Map<String, Payment> = emptyMap(),
    val users: Map<String, String> = emptyMap(),
    val simplifyDebts: Boolean = false,
    val currentDebts: Map<String, Map<String, Double>> = emptyMap(),
    val activityLog: Map<String, ActivityLog> = emptyMap(),
)