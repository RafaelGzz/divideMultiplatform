package com.ragl.divide.domain.services

import com.ragl.divide.data.models.Frequency

interface ScheduleNotificationService {
    /**
     * Programa una notificación para una fecha específica
     */
    fun scheduleNotification(
        id: Int,
        title: String,
        message: String,
        startingDateMillis: Long,
        frequency: Frequency,
        useSound: Boolean
    )

    fun canScheduleExactAlarms(): Boolean
    fun requestScheduleExactAlarmPermission()
    fun cancelNotification(id: Int)
    fun cancelAllNotifications()
    fun hasNotificationPermission(): Boolean
    fun requestNotificationPermission()
    fun wasNotificationPermissionRejectedPermanently(): Boolean
} 