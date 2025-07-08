package com.ragl.divide.data.services

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.ragl.divide.data.models.Frequency
import com.ragl.divide.data.models.getInMillis
import com.ragl.divide.domain.services.ScheduleNotificationService
import com.ragl.divide.presentation.utils.formatDate
import com.ragl.divide.presentation.utils.logMessage
import java.util.Calendar
import java.util.TimeZone

actual class ScheduleNotificationServiceImpl(
    private val context: Context
): ScheduleNotificationService {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val logTag = "ScheduleNotification"

    actual override fun canScheduleExactAlarms() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }

    actual override fun hasNotificationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    actual override fun wasNotificationPermissionRejectedPermanently(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationPermissionActivity.wasPermissionRejectedPermanently(context)
        } else {
            false
        }

    actual override fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Verificar si el permiso fue rechazado permanentemente
            if (NotificationPermissionActivity.wasPermissionRejectedPermanently(context)) {
                // Si fue rechazado permanentemente, abrir configuración directamente
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            } else {
                // Si no fue rechazado permanentemente, mostrar el diálogo
                val intent = Intent(context, NotificationPermissionActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }

    actual override fun requestScheduleExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = "package:${context.packageName}".toUri()
            }
            context.startActivity(intent)
        }
    }

    actual override fun scheduleNotification(
        id: Int,
        title: String,
        message: String,
        startingDateMillis: Long,
        frequency: Frequency,
        useSound: Boolean
    ) {
        val intent = Intent(context, Notifications::class.java).apply {
            putExtra(Notifications.TITLE_EXTRA, title)
            putExtra(Notifications.CONTENT_EXTRA, message)
            putExtra(Notifications.NOTIFICATION_ID_EXTRA, id)
            putExtra(Notifications.USE_SOUND_EXTRA, useSound)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Verificar si la fecha está en el pasado
        val now = System.currentTimeMillis()
        var finalStartingDateMillis = startingDateMillis
        
        if (finalStartingDateMillis <= now && frequency == Frequency.ONCE) {
            val calendar = Calendar.getInstance(TimeZone.getDefault())
            calendar.timeInMillis = now
            calendar.add(Calendar.MINUTE, 1)
            finalStartingDateMillis = calendar.timeInMillis
            logMessage(logTag, "¡Aviso! Fecha en pasado, ajustada a: ${formatDate(finalStartingDateMillis)}")
        }
        
        // Log básico sin tanta verbosidad
        logMessage(logTag, "Programando notificación $id para: ${formatDate(finalStartingDateMillis)}")

        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true

        if (canScheduleExact) {
            if (frequency == Frequency.ONCE) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    finalStartingDateMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    finalStartingDateMillis,
                    frequency.getInMillis(),
                    pendingIntent
                )
            }
            logMessage(logTag, "Notificación $id programada correctamente")
        } else {
            logMessage(logTag, "No se pudo programar la notificación $id (permiso denegado)")
        }
        
        //enableBootReceiver()
    }

    private fun enableBootReceiver() {
        val receiver = ComponentName(context, BootReminderNotificationsReceiver::class.java)
        val componentState = context.packageManager.getComponentEnabledSetting(receiver)
        
        if (componentState != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            context.packageManager.setComponentEnabledSetting(
                receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }

    actual override fun cancelNotification(id: Int) {
        val intent = Intent(context, Notifications::class.java)

        // Es importante recrear el PendingIntent exactamente igual que cuando se creó
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            // FLAG_NO_CREATE nos permite verificar si el pendingIntent existe
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let { existingIntent ->
            alarmManager.cancel(existingIntent)
            existingIntent.cancel() // Importante liberar el PendingIntent
            logMessage(logTag, "Notificación $id cancelada")
        }
    }

    actual override fun cancelAllNotifications() {
        try {
            // Cancelar todas las notificaciones mostradas
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancelAll()
            logMessage(logTag, "Todas las notificaciones mostradas canceladas")
        } catch (e: Exception) {
            logMessage(logTag, "Error al cancelar todas las notificaciones: $e")
        }
    }
}