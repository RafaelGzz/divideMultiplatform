package com.ragl.divide.data.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import com.ragl.divide.data.models.Frequency
import com.ragl.divide.data.models.getInMillis
import com.ragl.divide.ui.utils.formatDate
import com.ragl.divide.ui.utils.logMessage
import java.util.Calendar
import java.util.TimeZone

actual class ScheduleNotificationService(
    private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val logTag = "ScheduleNotification"

    actual fun canScheduleExactAlarms() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }

    actual fun requestScheduleExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = "package:${context.packageName}".toUri()
            }
            context.startActivity(intent)
        }
    }

    actual fun scheduleNotification(
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

    actual fun cancelNotification(id: Int) {
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
}