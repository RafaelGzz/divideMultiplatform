package com.ragl.divide.ui.utils

import android.util.Log
import android.content.Context
import android.widget.Toast
import com.ragl.divide.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun logMessage(tag: String, message: String) {
    Log.d(tag, message)
}

actual fun formatCurrency(value: Double, local: String): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag(local))
    return formatter.format(value)
}

actual fun formatDate(epochMilliseconds: Long, pattern: String): String {
    val date = Date(epochMilliseconds)
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
}

actual class NotificationStrings(private val context: Context) {
    actual fun getNotificationTitleString(title: String): String {
        // Obtiene el string desde los recursos y formatea con el parámetro 'title'
        return context.getString(R.string.notification_title, title)
    }

    actual fun getNotificationBodyString(): String {
        // Obtiene el string para el cuerpo de la notificación
        return context.getString(R.string.notification_body)
    }
}