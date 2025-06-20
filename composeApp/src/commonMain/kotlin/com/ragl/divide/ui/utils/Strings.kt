package com.ragl.divide.ui.utils

expect class Strings {
    fun getAppName(): String
    fun getNotificationBodyString(title: String): String
    fun getTwoSelected(): String
    fun getPercentagesSum(): String
    fun getTwoMustPay(): String
    fun getSumMustBe(amount: String): String
    fun getEmailRequired(): String
    fun getEmailNotValid(): String
    fun getPasswordRequired(): String
    fun getEmailAddressRequired(): String
    fun getInvalidEmailAddress(): String
    fun getUsernameEmpty(): String
    fun getUsernameRequirements(): String
    fun getPasswordMinLength(): String
    fun getPasswordRequirements(): String
    fun getPasswordsNotMatch(): String
    fun getSomethingWentWrong(): String
    fun getTitleRequired(): String
    fun getAmountRequired(): String
    fun getInvalidAmount(): String
    fun getAmountMustBeGreater(): String
    fun getUnknownError(): String
    fun getErrorDeletingPayment(): String
    fun getEmailPasswordInvalid(): String
    fun getEmailAlreadyInUse(): String
    fun getFailedToLogin(): String
    fun getEmailNotVerified(): String
    fun getVerificationEmailSent(): String
    fun getUnusualActivity(): String
    fun getCannotDeleteGroup(): String
    fun getCannotLeaveGroup(): String
    fun getCannotDeleteEvent(): String
    fun getCouldNotProcessImage(): String
    fun getFriendRequestSent(): String
    fun getFriendRequestAccepted(): String
    fun getFriendRequestRejected(): String
    fun getFriendRequestCanceled(): String
    fun getFailedToSendFriendRequest(): String
    fun getFailedToAcceptFriendRequest(): String
    fun getFailedToRejectFriendRequest(): String
    fun getFailedToCancelFriendRequest(): String
    fun getFriendRemoved(): String
    fun getFailedToRemoveFriend(): String
    fun getExpenseAlreadyPaid(): String
    fun getCongratulations(title: String): String
    fun getDescriptionTooLong(): String
    fun getCannotModifyWhileEventSettled(): String
}