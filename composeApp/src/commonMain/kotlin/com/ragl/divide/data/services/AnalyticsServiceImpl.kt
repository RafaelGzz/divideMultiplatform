package com.ragl.divide.data.services

import com.ragl.divide.domain.services.AnalyticsService
import dev.gitlive.firebase.analytics.FirebaseAnalytics
import dev.gitlive.firebase.analytics.logEvent
import dev.gitlive.firebase.crashlytics.FirebaseCrashlytics

class AnalyticsServiceImpl(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) : AnalyticsService {

    override fun setUserProperties(userId: String, userName: String) {
        crashlytics.setUserId(userId)
        crashlytics.setCustomKey("user_name", userName)
    }

    override fun logEvent(eventName: String, params: Map<String, Any>) {
        analytics.logEvent(eventName) {
            params.forEach { (key, value) ->
                param(key, value.toString())
            }
        }
    }

    override fun logError(throwable: Throwable, message: String?) {
        message?.let { crashlytics.setCustomKey("error_message", it) }
        crashlytics.recordException(throwable)
    }
} 