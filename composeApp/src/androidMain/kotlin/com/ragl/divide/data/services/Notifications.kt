package com.ragl.divide.data.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ragl.divide.presentation.MainActivity
import com.ragl.divide.R

class Notifications : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "reminder_channel"
        const val TITLE_EXTRA = "titleExtra"
        const val CONTENT_EXTRA = "contentExtra"
        const val NOTIFICATION_ID_EXTRA = "notificationIdExtra"
        const val USE_SOUND_EXTRA = "useSoundExtra"
    }

    private fun isNotificationsPermissionGranted(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

    private fun showNotification(
        context: Context,
        title: String = "",
        content: String = "",
        notificationId: Int = 1,
        useSound: Boolean = true,
        icon: Int = R.drawable.ic_divide
    ) {
        if (!isNotificationsPermissionGranted(context)) {
            return
        }

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(activityPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (useSound) {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            notificationBuilder.setSound(defaultSoundUri)
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal para Android O y versiones posteriores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para notificaciones de recordatorios de pagos"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(
            notificationId, notificationBuilder.build()
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(NOTIFICATION_ID_EXTRA, 1)
        val useSound = intent.getBooleanExtra(USE_SOUND_EXTRA, true)
        
        showNotification(
            context,
            intent.getStringExtra(TITLE_EXTRA) ?: "",
            intent.getStringExtra(CONTENT_EXTRA) ?: "",
            notificationId,
            useSound
        )
    }
}