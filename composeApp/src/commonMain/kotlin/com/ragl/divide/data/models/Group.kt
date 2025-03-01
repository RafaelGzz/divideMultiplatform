package com.ragl.divide.data.models

data class Group(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val expenses: Map<String, GroupExpense> = emptyMap(),
    val payments: Map<String, Payment> = emptyMap(),
    val users: Map<String, GroupUser> = emptyMap(),
)