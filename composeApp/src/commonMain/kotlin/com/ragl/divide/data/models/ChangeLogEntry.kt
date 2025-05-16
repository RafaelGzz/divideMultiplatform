package com.ragl.divide.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ChangeLogEntry(
    val field: String,
    val oldValue: String,
    val newValue: String
)