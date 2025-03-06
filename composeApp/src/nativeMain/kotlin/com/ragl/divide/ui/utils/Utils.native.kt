package com.ragl.divide.ui.utils

import platform.Foundation.*

actual fun logMessage(tag: String, message: String) {
    println("$tag: $message")
}

actual fun formatCurrency(value: Double, local: String): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
        locale = NSLocale(local)
    }
    return formatter.stringFromNumber(NSNumber(value)) ?: "$value"
}

actual fun formatDate(epochMilliseconds: Long, pattern: String): String {
    val date = NSDate.dateWithTimeIntervalSince1970(epochMilliseconds.toDouble() / 1000)
    
    val formatter = NSDateFormatter().apply {
        dateFormat = pattern
        locale = NSLocale.currentLocale
    }
    
    return formatter.stringFromDate(date) ?: "$epochMilliseconds"
}

actual class NotificationStrings {
    actual fun getNotificationTitleString(title: String): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey(title, title, null)
    }
    actual fun getNotificationBodyString(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("notification_body", "", null)
    }
}