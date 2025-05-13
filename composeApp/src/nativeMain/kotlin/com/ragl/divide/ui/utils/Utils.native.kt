package com.ragl.divide.ui.utils

import platform.Foundation.NSBundle
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

actual class Strings {
    actual fun getNotificationTitleString(title: String): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey(title, title, null)
    }
    actual fun getNotificationBodyString(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("notification_body", "", null)
    }

    actual fun getTwoSelected(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("two_people_must_be_selected", "", null)
    }

    actual fun getPercentagesSum(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("percentages_sum_must_be_100", "", null)
    }

    actual fun getTwoMustPay(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("two_people_must_pay", "", null)
    }

    actual fun getSumMustBe(amount: String): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("quantities_sum_must_be_amount", amount, null)
    }
}