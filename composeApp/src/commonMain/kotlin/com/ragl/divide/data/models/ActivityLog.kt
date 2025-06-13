package com.ragl.divide.data.models

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class ActivityLog(
    val type: LogType,
    val refId: String,
    val userId: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val changes: List<ChangeLogEntry> = emptyList()
)

enum class LogType{
    EXPENSE_CREATED,
    EXPENSE_UPDATED,
    EXPENSE_DELETED,
    PAYMENT_CREATED,
    PAYMENT_UPDATED,
    PAYMENT_DELETED,
    USER_ADDED,
    USER_REMOVED,
    GROUP_CREATED,
    GROUP_DELETED,
    GROUP_UPDATED,
    EVENT_CREATED,
    EVENT_UPDATED,
    EVENT_DELETED,
    EVENT_SETTLED,
    EVENT_REOPENED,
}

@Serializable
data class ChangeLogEntry(
    val field: String,
    val oldValue: String,
    val newValue: String
)