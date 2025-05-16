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

