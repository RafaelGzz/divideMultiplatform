package com.ragl.divide.data.services

import com.ragl.divide.data.models.Frequency
import com.ragl.divide.data.models.getInMillis
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationTrigger
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

actual class ScheduleNotificationService {
    actual fun scheduleNotification(
        id: Int,
        title: String,
        message: String,
        startingDateMillis: Long,
        frequency: Frequency
    ) {
        val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

        notificationCenter.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
            completionHandler = { granted, error ->
                if (granted) {
                    val content = UNMutableNotificationContent()
                    content.setTitle(title)
                    content.setBody(message)

                    val trigger: UNNotificationTrigger?
                    if(frequency == Frequency.ONCE){
                        val triggerTime = startingDateMillis - NSDate().timeIntervalSince1970
                        trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(triggerTime, false)
                    } else {
                        val triggerTime = frequency.getInMillis().toDouble()
                        trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(triggerTime, true)
                    }

                    val request = UNNotificationRequest.requestWithIdentifier(
                        "$id",
                        content,
                        trigger
                    )

                    notificationCenter.addNotificationRequest(request, null)
                }
            }
        )
    }

    actual fun canScheduleExactAlarms(): Boolean = true

    actual fun requestScheduleExactAlarmPermission() {
    }

    actual fun cancelNotification(id: Int) {
        val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(listOf("$id"))
        notificationCenter.removeDeliveredNotificationsWithIdentifiers(listOf("$id"))
    }

}