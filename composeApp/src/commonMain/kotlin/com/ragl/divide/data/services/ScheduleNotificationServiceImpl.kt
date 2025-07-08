package com.ragl.divide.data.services

import com.ragl.divide.data.models.Frequency
import com.ragl.divide.domain.services.ScheduleNotificationService

expect class ScheduleNotificationServiceImpl: ScheduleNotificationService {
    override fun scheduleNotification(
        id: Int,
        title: String,
        message: String,
        startingDateMillis: Long,
        frequency: Frequency,
        useSound: Boolean
    )

    override fun canScheduleExactAlarms(): Boolean
    override fun requestScheduleExactAlarmPermission()
    override fun cancelNotification(id: Int)
    override fun cancelAllNotifications()
    override fun hasNotificationPermission(): Boolean
    override fun requestNotificationPermission()
    override fun wasNotificationPermissionRejectedPermanently(): Boolean
}