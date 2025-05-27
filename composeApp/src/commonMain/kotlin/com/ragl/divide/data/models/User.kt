package com.ragl.divide.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uuid: String = "",
    val email: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val groups: Map<String, String> = emptyMap(),
    val expenses: Map<String, Expense> = emptyMap(),
    val friends: Map<String, String> = emptyMap(),
    val friendRequestsReceived: Map<String, String> = emptyMap(),
    val friendRequestsSent: Map<String, String> = emptyMap()
)