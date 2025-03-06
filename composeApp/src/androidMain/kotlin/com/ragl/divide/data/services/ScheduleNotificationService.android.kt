package com.ragl.divide.data.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.ragl.divide.data.models.Frequency
import com.ragl.divide.data.models.getInMillis
import com.ragl.divide.ui.utils.formatDate
import com.ragl.divide.ui.utils.logMessage

actual class ScheduleNotificationService(
    private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    actual fun canScheduleExactAlarms() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }

    actual fun requestScheduleExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }

    actual fun scheduleNotification(
        id: Int,
        title: String,
        message: String,
        startingDateMillis: Long,
        frequency: Frequency
    ) {
        val intent = Intent(context, Notifications::class.java).apply {
            putExtra(Notifications.TITLE_EXTRA, title)
            putExtra(Notifications.CONTENT_EXTRA, message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                if (frequency == Frequency.ONCE) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        startingDateMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        startingDateMillis,
                        frequency.getInMillis(),
                        pendingIntent
                    )
                }
            }
        } else {
            if (frequency == Frequency.ONCE) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    startingDateMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    startingDateMillis,
                    frequency.getInMillis(),
                    pendingIntent
                )
            }
        }
        logMessage("ScheduleNotificationService", "Notificación programada para el ${formatDate(startingDateMillis)}")
        enableBootReceiver()
    }

    private fun enableBootReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.setComponentEnabledSetting(
                ComponentName(context, BootReminderNotificationsReceiver::class.java),
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
        }
    }
}