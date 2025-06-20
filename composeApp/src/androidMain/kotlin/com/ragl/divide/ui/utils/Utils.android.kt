package com.ragl.divide.ui.utils

import android.util.Log
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