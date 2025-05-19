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
    
    actual fun getEmailRequired(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("email_required", "", null)
    }
    
    actual fun getEmailNotValid(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("email_not_valid", "", null)
    }
    
    actual fun getPasswordRequired(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("password_required", "", null)
    }
    
    actual fun getEmailAddressRequired(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("email_address_required", "", null)
    }
    
    actual fun getInvalidEmailAddress(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("invalid_email_address", "", null)
    }
    
    actual fun getUsernameEmpty(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("username_empty", "", null)
    }
    
    actual fun getUsernameRequirements(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("username_requirements", "", null)
    }
    
    actual fun getPasswordMinLength(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("password_min_length", "", null)
    }
    
    actual fun getPasswordRequirements(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("password_requirements", "", null)
    }
    
    actual fun getPasswordsNotMatch(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("passwords_not_match", "", null)
    }
    
    actual fun getSomethingWentWrong(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("something_went_wrong", "", null)
    }
    
    actual fun getTitleRequired(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("title_required", "", null)
    }
    
    actual fun getAmountRequired(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("amount_required", "", null)
    }
    
    actual fun getInvalidAmount(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("invalid_amount", "", null)
    }
    
    actual fun getAmountMustBeGreater(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("amount_must_be_greater", "", null)
    }
    
    actual fun getUnknownError(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("unknown_error", "", null)
    }
    
    actual fun getErrorDeletingPayment(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("error_deleting_payment", "", null)
    }
    
    actual fun getEmailPasswordInvalid(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("email_password_invalid", "", null)
    }
    
    actual fun getEmailAlreadyInUse(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("email_already_in_use", "", null)
    }
    
    actual fun getFailedToLogin(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("failed_to_login", "", null)
    }

    actual fun getEmailNotVerified(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("email_not_verified", "", null)
    }

    actual fun getVerificationEmailSent(): String {
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("verification_email_sent", "", null)
    }

    actual fun getUnusualActivity(): String{
        val bundle = NSBundle.mainBundle
        return bundle.localizedStringForKey("unusual_activity", "", null)
    }
}