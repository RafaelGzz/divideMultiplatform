package com.ragl.divide.ui.utils

import android.content.Context
import android.util.Log
import com.ragl.divide.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

actual fun logMessage(tag: String, message: String) {
    Log.d(tag, message)
}

actual fun formatCurrency(value: Double, local: String): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag(local))
    return formatter.format(value)
}

actual fun formatDate(epochMilliseconds: Long, pattern: String): String {
    val date = Date(epochMilliseconds)
    val formatter = SimpleDateFormat(pattern, Locale.getDefault()).apply {
        // Establecer explícitamente la zona horaria local para evitar problemas con diferentes zonas
        timeZone = TimeZone.getDefault()
    }
    return formatter.format(date)
}

actual class Strings(private val context: Context) {
    actual fun getNotificationTitleString(title: String): String {
        // Obtiene el string desde los recursos y formatea con el parámetro 'title'
        return context.getString(R.string.notification_title, title)
    }

    actual fun getNotificationBodyString(): String {
        // Obtiene el string para el cuerpo de la notificación
        return context.getString(R.string.notification_body)
    }

    actual fun getTwoSelected(): String {
        return context.getString(R.string.two_people_must_be_selected)
    }

    actual fun getPercentagesSum(): String {
        return context.getString(R.string.percentages_sum_must_be_100)
    }

    actual fun getTwoMustPay(): String {
        return context.getString(R.string.two_people_must_pay)
    }

    actual fun getSumMustBe(amount: String): String {
        return context.getString(R.string.quantities_sum_must_be_amount, amount)
    }
}