package com.ragl.divide.data.services

import com.ragl.divide.data.models.Frequency

expect class ScheduleNotificationService {
    fun scheduleNotification(
        id: Int,
        title: String,
        message: String,
        startingDateMillis: Long,
        frequency: Frequency
    )

    fun canScheduleExactAlarms(): Boolean
    fun requestScheduleExactAlarmPermission()
    fun cancelNotification(id: Int)
}