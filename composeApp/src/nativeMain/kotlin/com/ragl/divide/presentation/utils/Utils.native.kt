package com.ragl.divide.presentation.utils

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone

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
    val epochSeconds = epochMilliseconds.toDouble() / 1000
    val date = NSDate.dateWithTimeIntervalSince1970(epochSeconds)

    val formatter = NSDateFormatter().apply {
        dateFormat = pattern
        locale = NSLocale.currentLocale
        timeZone = NSTimeZone.localTimeZone
    }

    return formatter.stringFromDate(date) ?: "$epochMilliseconds"
}