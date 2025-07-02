package com.ragl.divide.ui.utils

import platform.Foundation.NSBundle
import kotlin.random.Random

actual class Strings {
    private val bundle = NSBundle.mainBundle

    actual fun getAppName(): String {
        return bundle.localizedStringForKey("app_name", "Divide", null)
    }

    actual fun getNotificationBodyString(title: String): String {
        // Lista de claves de mensajes de recordatorio
        val reminderKeys = listOf(
            "notification_reminder_1",
            "notification_reminder_2",
            "notification_reminder_3",
            "notification_reminder_4",
            "notification_reminder_5",
            "notification_reminder_6"
        )

        // Seleccionar una clave aleatoria
        val randomKey = reminderKeys[Random.nextInt(reminderKeys.size)]

        // Obtener el mensaje y reemplazar el marcador de posición
        val messageTemplate = bundle.localizedStringForKey(
            randomKey,
            "Remember to pay $title!",
            null
        )

        return messageTemplate.replace("%1\$s", title)
    }

    actual fun getTwoSelected(): String {
        return bundle.localizedStringForKey("two_people_must_be_selected", "At least two people must be selected", null)
    }

    actual fun getPercentagesSum(): String {
        return bundle.localizedStringForKey("percentages_sum_must_be_100", "Percentage sum must be 100%", null)
    }

    actual fun getTwoMustPay(): String {
        return bundle.localizedStringForKey("two_people_must_pay", "At least two people must pay", null)
    }

    actual fun getSumMustBe(amount: String): String {
        return bundle.localizedStringForKey("quantities_sum_must_be_amount", "Quantities sum must be $amount", null)
    }

    actual fun getEmailRequired(): String {
        return bundle.localizedStringForKey("email_required", "Email is required", null)
    }

    actual fun getEmailNotValid(): String {
        return bundle.localizedStringForKey("email_not_valid", "Email is not valid", null)
    }

    actual fun getPasswordRequired(): String {
        return bundle.localizedStringForKey("password_required", "Password is required", null)
    }

    actual fun getEmailAddressRequired(): String {
        return bundle.localizedStringForKey("email_address_required", "Email Address is required", null)
    }

    actual fun getInvalidEmailAddress(): String {
        return bundle.localizedStringForKey("invalid_email_address", "Invalid email address", null)
    }

    actual fun getUsernameEmpty(): String {
        return bundle.localizedStringForKey("username_empty", "Username cannot be empty", null)
    }

    actual fun getUsernameRequirements(): String {
        return bundle.localizedStringForKey("username_requirements", "Username must be between 3 and 20 characters and can only contain letters, numbers, underscores, and hyphens", null)
    }

    actual fun getPasswordMinLength(): String {
        return bundle.localizedStringForKey("password_min_length", "Password must be at least 8 characters", null)
    }

    actual fun getPasswordRequirements(): String {
        return bundle.localizedStringForKey("password_requirements", "Password must contain at least one number, one uppercase letter, one lowercase letter and one special character", null)
    }

    actual fun getPasswordsNotMatch(): String {
        return bundle.localizedStringForKey("passwords_not_match", "Passwords do not match", null)
    }

    actual fun getSomethingWentWrong(): String {
        return bundle.localizedStringForKey("something_went_wrong", "Something went wrong", null)
    }

    actual fun getTitleRequired(): String {
        return bundle.localizedStringForKey("title_required", "Title is required", null)
    }

    actual fun getAmountRequired(): String {
        return bundle.localizedStringForKey("amount_required", "Amount is required", null)
    }

    actual fun getInvalidAmount(): String {
        return bundle.localizedStringForKey("invalid_amount", "Invalid amount", null)
    }

    actual fun getAmountMustBeGreater(): String {
        return bundle.localizedStringForKey("amount_must_be_greater", "Amount must be greater than 0", null)
    }

    actual fun getUnknownError(): String {
        return bundle.localizedStringForKey("unknown_error", "Unknown error", null)
    }

    actual fun getErrorDeletingPayment(): String {
        return bundle.localizedStringForKey("error_deleting_payment", "Error deleting payment", null)
    }

    actual fun getEmailPasswordInvalid(): String {
        return bundle.localizedStringForKey("email_password_invalid", "The email or password is invalid", null)
    }

    actual fun getEmailAlreadyInUse(): String {
        return bundle.localizedStringForKey("email_already_in_use", "The email address is already in use by another account", null)
    }

    actual fun getFailedToLogin(): String {
        return bundle.localizedStringForKey("failed_to_login", "Failed to Log in", null)
    }

    actual fun getEmailNotVerified(): String {
        return bundle.localizedStringForKey("email_not_verified", "Email not verified", null)
    }

    actual fun getVerificationEmailSent(): String {
        return bundle.localizedStringForKey("verification_email_sent", "Verification email sent", null)
    }

    actual fun getUnusualActivity(): String{
        return bundle.localizedStringForKey("unusual_activity", "Unusual activity detected. Try again later.", null)
    }

    actual fun getCannotDeleteGroup(): String {
        return bundle.localizedStringForKey("cannot_delete_group", "Cannot delete group", null)
    }

    actual fun getCannotLeaveGroup(): String {
        return bundle.localizedStringForKey("cannot_leave_group", "Cannot leave group", null)
    }

    actual fun getCannotDeleteEvent(): String {
        return bundle.localizedStringForKey("cannot_delete_event", "Cannot delete event", null)
    }

    actual fun getCouldNotProcessImage(): String {
        return bundle.localizedStringForKey("could_not_process_image", "Could not process the image", null)
    }

    actual fun getFriendRequestSent(): String {
        return bundle.localizedStringForKey("friend_request_sent", "Friend request sent successfully", null)
    }

    actual fun getFriendRequestAccepted(): String {
        return bundle.localizedStringForKey("friend_request_accepted", "Friend request accepted", null)
    }

    actual fun getFriendRequestRejected(): String {
        return bundle.localizedStringForKey("friend_request_rejected", "Friend request rejected", null)
    }

    actual fun getFriendRequestCanceled(): String {
        return bundle.localizedStringForKey("friend_request_canceled", "Friend request canceled", null)
    }

    actual fun getFailedToSendFriendRequest(): String {
        return bundle.localizedStringForKey("failed_to_send_friend_request", "Failed to send friend request", null)
    }

    actual fun getFailedToAcceptFriendRequest(): String {
        return bundle.localizedStringForKey("failed_to_accept_friend_request", "Failed to accept friend request", null)
    }

    actual fun getFailedToRejectFriendRequest(): String {
        return bundle.localizedStringForKey("failed_to_reject_friend_request", "Failed to reject friend request", null)
    }

    actual fun getFailedToCancelFriendRequest(): String {
        return bundle.localizedStringForKey("failed_to_cancel_friend_request", "Failed to cancel friend request", null)
    }

    actual fun getFriendRemoved(): String {
        return bundle.localizedStringForKey("friend_removed", "Friend removed", null)
    }

    actual fun getFailedToRemoveFriend(): String {
        return bundle.localizedStringForKey("failed_to_remove_friend", "Failed to remove friend", null)
    }

    actual fun getExpenseAlreadyPaid(): String {
        return bundle.localizedStringForKey("expense_already_paid", "This expense has already been paid", null)
    }

    actual fun getCongratulations(title: String): String {
        return bundle.localizedStringForKey("congratulations", "Congratulations! You finished paying $title", null)
    }

    actual fun getDescriptionTooLong(): String {
        return bundle.localizedStringForKey("description_too_long", "Description is too long", null)
    }

    actual fun getCannotModifyWhileEventSettled(): String {
        return bundle.localizedStringForKey("cannot_modify_while_event_settled", "Cannot modify this item while the event is settled", null)
    }

    actual fun getNameRequired(): String {
        return bundle.localizedStringForKey("name_required", "Name is required", null)
    }

    actual fun getNameTooLong(): String {
        return bundle.localizedStringForKey("name_too_long", "Name cannot be longer than 20 characters", null)
    }

    actual fun getNameCannotContainSpaces(): String {
        return bundle.localizedStringForKey("name_cannot_contain_spaces", "Name cannot contain spaces", null)
    }

    actual fun getNameAlreadyExists(): String {
        return bundle.localizedStringForKey("name_already_exists", "This name already exists in the group", null)
    }

    actual fun getCannotRemoveLastMember(): String {
        return bundle.localizedStringForKey("cannot_remove_last_member", "Cannot remove the last member of the group", null)
    }

    actual fun getCannotRemoveGuestWithDebts(): String {
        return bundle.localizedStringForKey("cannot_remove_guest_with_debts", "Cannot remove guest with active debts", null)
    }

}