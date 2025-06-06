package com.ragl.divide.ui.utils

import android.content.Context
import android.util.Log
import com.ragl.divide.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random

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

    actual fun getAppName(): String{
        return context.getString(R.string.app_name)
    }

    fun getNotificationTitleString(title: String): String {
        // Obtiene el string desde los recursos y formatea con el parámetro 'title'
        return context.getString(R.string.notification_title, title)
    }

    actual fun getNotificationBodyString(title: String): String {
        // Obtiene un mensaje aleatorio de recordatorio de pago
        val reminderMessages = arrayOf(
            R.string.notification_reminder_1,
            R.string.notification_reminder_2,
            R.string.notification_reminder_3,
            R.string.notification_reminder_4,
            R.string.notification_reminder_5,
            R.string.notification_reminder_6
        )
        
        val randomMessageId = reminderMessages[Random.nextInt(reminderMessages.size)]
        return context.getString(randomMessageId, title)
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
    
    actual fun getEmailRequired(): String {
        return context.getString(R.string.email_required)
    }
    
    actual fun getEmailNotValid(): String {
        return context.getString(R.string.email_not_valid)
    }
    
    actual fun getPasswordRequired(): String {
        return context.getString(R.string.password_required)
    }
    
    actual fun getEmailAddressRequired(): String {
        return context.getString(R.string.email_address_required)
    }
    
    actual fun getInvalidEmailAddress(): String {
        return context.getString(R.string.invalid_email_address)
    }
    
    actual fun getUsernameEmpty(): String {
        return context.getString(R.string.username_empty)
    }
    
    actual fun getUsernameRequirements(): String {
        return context.getString(R.string.username_requirements)
    }
    
    actual fun getPasswordMinLength(): String {
        return context.getString(R.string.password_min_length)
    }
    
    actual fun getPasswordRequirements(): String {
        return context.getString(R.string.password_requirements)
    }
    
    actual fun getPasswordsNotMatch(): String {
        return context.getString(R.string.passwords_not_match)
    }
    
    actual fun getSomethingWentWrong(): String {
        return context.getString(R.string.something_went_wrong)
    }
    
    actual fun getTitleRequired(): String {
        return context.getString(R.string.title_required)
    }
    
    actual fun getAmountRequired(): String {
        return context.getString(R.string.amount_required)
    }
    
    actual fun getInvalidAmount(): String {
        return context.getString(R.string.invalid_amount)
    }
    
    actual fun getAmountMustBeGreater(): String {
        return context.getString(R.string.amount_must_be_greater)
    }
    
    actual fun getUnknownError(): String {
        return context.getString(R.string.unknown_error)
    }
    
    actual fun getErrorDeletingPayment(): String {
        return context.getString(R.string.error_deleting_payment)
    }
    
    actual fun getEmailPasswordInvalid(): String {
        return context.getString(R.string.email_password_invalid)
    }
    
    actual fun getEmailAlreadyInUse(): String {
        return context.getString(R.string.email_already_in_use)
    }
    
    actual fun getFailedToLogin(): String {
        return context.getString(R.string.failed_to_login)
    }

    actual fun getUnusualActivity(): String {
        return context.getString(R.string.unusual_activity)
    }

    actual fun getEmailNotVerified(): String {
        return context.getString(R.string.email_not_verified)
    }

    actual fun getVerificationEmailSent(): String {
        return context.getString(R.string.verification_email_sent)
    }

    actual fun getCannotDeleteGroup(): String {
        return context.getString(R.string.cannot_delete_group)
    }

    actual fun getCannotLeaveGroup(): String {
        return context.getString(R.string.cannot_leave_group)
    }

    actual fun getCannotDeleteEvent(): String {
        return context.getString(R.string.cannot_delete_event)
    }

    actual fun getCouldNotProcessImage(): String {
        return context.getString(R.string.could_not_process_image)
    }
    
    actual fun getFriendRequestSent(): String {
        return context.getString(R.string.friend_request_sent)
    }
    
    actual fun getFriendRequestAccepted(): String {
        return context.getString(R.string.friend_request_accepted)
    }
    
    actual fun getFriendRequestRejected(): String {
        return context.getString(R.string.friend_request_rejected)
    }
    
    actual fun getFriendRequestCanceled(): String {
        return context.getString(R.string.friend_request_canceled)
    }
    
    actual fun getFailedToSendFriendRequest(): String {
        return context.getString(R.string.failed_to_send_friend_request)
    }
    
    actual fun getFailedToAcceptFriendRequest(): String {
        return context.getString(R.string.failed_to_accept_friend_request)
    }
    
    actual fun getFailedToRejectFriendRequest(): String {
        return context.getString(R.string.failed_to_reject_friend_request)
    }
    
    actual fun getFailedToCancelFriendRequest(): String {
        return context.getString(R.string.failed_to_cancel_friend_request)
    }

    actual fun getFriendRemoved(): String {
        return context.getString(R.string.friend_removed)
    }

    actual fun getFailedToRemoveFriend(): String {
        return context.getString(R.string.failed_to_remove_friend)
    }

    actual fun getExpenseAlreadyPaid(): String {
        return context.getString(R.string.expense_already_paid)
    }

    actual fun getCongratulations(title: String): String {
        return context.getString(R.string.congratulations, title)
    }
}