package com.ragl.divide.data.services

import dev.gitlive.firebase.analytics.FirebaseAnalytics
import dev.gitlive.firebase.analytics.logEvent
import dev.gitlive.firebase.crashlytics.FirebaseCrashlytics

class AnalyticsService(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) {

    fun setUserProperties(userId: String, userName: String) {
        crashlytics.setUserId(userId)
        crashlytics.setCustomKey("user_name", userName)
    }

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        analytics.logEvent(eventName) {
            params.forEach { (key, value) ->
                param(key, value.toString())
            }
        }
    }

    fun logError(throwable: Throwable, message: String? = null) {
        message?.let { crashlytics.setCustomKey("error_message", it) }
        crashlytics.recordException(throwable)
    }

    // Eventos específicos de la app
    fun logExpenseCreated(expenseId: String, amount: Double) {
        logEvent("expense_created", mapOf(
            "expense_id" to expenseId,
            "amount" to amount
        ))
    }

    fun logPaymentMade(paymentId: String, amount: Double) {
        logEvent("payment_made", mapOf(
            "payment_id" to paymentId,
            "amount" to amount
        ))
    }

    fun logGroupCreated(groupId: String, memberCount: Int) {
        logEvent("group_created", mapOf(
            "group_id" to groupId,
            "member_count" to memberCount
        ))
    }

    fun logFriendAdded(friendId: String) {
        logEvent("friend_added", mapOf(
            "friend_id" to friendId
        ))
    }
} 