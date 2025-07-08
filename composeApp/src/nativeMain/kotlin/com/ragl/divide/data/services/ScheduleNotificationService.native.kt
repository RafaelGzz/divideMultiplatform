package com.ragl.divide.data.services

import com.ragl.divide.data.models.Frequency
import com.ragl.divide.data.models.getInMillis
import com.ragl.divide.domain.services.ScheduleNotificationService
import com.ragl.divide.presentation.utils.logMessage
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNNotificationTrigger
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

actual class ScheduleNotificationServiceImpl: ScheduleNotificationService {
    private val logTag = "ScheduleNotification"
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    
    actual override fun hasNotificationPermission(): Boolean {
        return true
    }
    
    actual override fun requestNotificationPermission() {
        requestNotificationPermission { granted ->
            logMessage(logTag, "Permisos de notificación solicitados: ${if (granted) "concedidos" else "denegados"}")
        }
    }
    
    actual override fun wasNotificationPermissionRejectedPermanently(): Boolean {
        // En iOS no hay concepto de "rechazado permanentemente" como en Android
        return false
    }
    
    actual override fun scheduleNotification(
        id: Int,
        title: String,
        message: String,
        startingDateMillis: Long,
        frequency: Frequency,
        useSound: Boolean
    ) {
        requestNotificationPermission { granted ->
            if (granted) {
                scheduleNotificationInternal(id, title, message, startingDateMillis, frequency, useSound)
            } else {
                logMessage(logTag, "No se pudo programar la notificación: permisos denegados")
            }
        }
    }
    
    private fun requestNotificationPermission(completion: (Boolean) -> Unit) {
        notificationCenter.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
            completionHandler = { granted, error ->
                if (error != null) {
                    logMessage(logTag, "Error al solicitar permisos: ${error.localizedDescription}")
                    completion(false)
                } else {
                    completion(granted)
                }
            }
        )
    }
    
    private fun scheduleNotificationInternal(
        id: Int,
        title: String,
        message: String,
        startingDateMillis: Long,
        frequency: Frequency,
        useSound: Boolean
    ) {
        try {
            val content = UNMutableNotificationContent().apply {
                setTitle(title)
                setBody(message)
                
                // Configurar sonido solo si se solicita
                if (useSound) {
                    setSound(UNNotificationSound.defaultSound)
                }
                
                // Añadir categoría para posibles acciones futuras
                setThreadIdentifier("expense_reminders")
                setBadge(NSNumber(1))
            }

            val trigger: UNNotificationTrigger?
            val startingTimeInSeconds = startingDateMillis / 1000.0
            val currentTimeInSeconds = NSDate().timeIntervalSince1970
            
            if (frequency == Frequency.ONCE) {
                var triggerTime = startingTimeInSeconds - currentTimeInSeconds
                
                if (triggerTime <= 0) {
                    // Ajustar a 1 minuto en el futuro para consistencia con Android
                    triggerTime = 60.0
                    val formattedDate = formatNotificationDate(NSDate().timeIntervalSince1970 + triggerTime)
                    logMessage(logTag, "¡Aviso! Fecha en pasado, ajustada a: $formattedDate")
                }
                
                logMessage(logTag, "Programando notificación $id para: ${formatNotificationDate(startingTimeInSeconds)}")
                trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(triggerTime, false)
            } else {
                val triggerTime = frequency.getInMillis() / 1000.0
                logMessage(logTag, "Programando notificación periódica $id, frecuencia: ${frequency.name}")
                trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(triggerTime, true)
            }

            val request = UNNotificationRequest.requestWithIdentifier(
                "$id",
                content,
                trigger
            )

            notificationCenter.addNotificationRequest(request) { error ->
                if (error != null) {
                    logMessage(logTag, "Error al programar la notificación: ${error.localizedDescription}")
                } else {
                    logMessage(logTag, "Notificación $id programada correctamente")
                }
            }
        } catch (e: Exception) {
            logMessage(logTag, "Error inesperado al programar notificación: $e")
        }
    }
    
    private fun formatNotificationDate(epochSeconds: Double): String {
        val date = NSDate.dateWithTimeIntervalSince1970(epochSeconds)
        val formatter = NSDateFormatter().apply {
            dateFormat = "dd/MM/yyyy HH:mm:ss"
            locale = NSLocale.currentLocale
            timeZone = NSTimeZone.localTimeZone
        }
        return formatter.stringFromDate(date) ?: "$epochSeconds"
    }

    actual override fun canScheduleExactAlarms(): Boolean = true

    actual override fun requestScheduleExactAlarmPermission() {
        // En iOS no es necesario un permiso adicional para alarmas exactas
        // Pero podemos solicitar los permisos de notificación de nuevo
        requestNotificationPermission { granted ->
            logMessage(logTag, "Permisos de notificación: ${if (granted) "concedidos" else "denegados"}")
        }
    }

    actual override fun cancelNotification(id: Int) {
        try {
            notificationCenter.removePendingNotificationRequestsWithIdentifiers(listOf("$id"))
            notificationCenter.removeDeliveredNotificationsWithIdentifiers(listOf("$id"))
            logMessage(logTag, "Notificación $id cancelada")
        } catch (e: Exception) {
            logMessage(logTag, "Error al cancelar notificación $id: $e")
        }
    }
    
    // Método para cancelar todas las notificaciones
    actual override fun cancelAllNotifications() {
        try {
            notificationCenter.removeAllPendingNotificationRequests()
            notificationCenter.removeAllDeliveredNotifications()
            logMessage(logTag, "Todas las notificaciones canceladas")
        } catch (e: Exception) {
            logMessage(logTag, "Error al cancelar todas las notificaciones: $e")
        }
    }
}
