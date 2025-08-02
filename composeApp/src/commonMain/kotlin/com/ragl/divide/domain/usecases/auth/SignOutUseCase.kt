package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.ScheduleNotificationService
class SignOutUseCase(
    private val userRepository: UserRepository,
    private val scheduleNotificationService: ScheduleNotificationService
) {
    sealed class Result {
        object Success : Result()
        data class Error(val exception: Exception) : Result()
    }

    suspend operator fun invoke(): Result {
        return try {
            scheduleNotificationService.cancelAllNotifications()
            userRepository.signOut()
            if (userRepository.getCurrentUser() == null) {
                Result.Success
            } else {
                Result.Error(Exception("Still logged in"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 