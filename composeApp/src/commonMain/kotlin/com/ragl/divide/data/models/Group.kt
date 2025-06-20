package com.ragl.divide.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val expenses: Map<String, GroupExpense> = emptyMap(),
    val payments: Map<String, GroupPayment> = emptyMap(),
    val users: Map<String, String> = emptyMap(),
    val simplifyDebts: Boolean = true,
    val events: Map<String, GroupEvent> = emptyMap(),
)