package com.ragl.divide.domain.services

import io.mockative.Mockable

@Mockable
interface AnalyticsService {
    fun setUserProperties(userId: String, userName: String)
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap())
    fun logError(throwable: Throwable, message: String? = null)
} 