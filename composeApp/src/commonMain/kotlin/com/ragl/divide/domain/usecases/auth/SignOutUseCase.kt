package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.ScheduleNotificationService
import com.ragl.divide.presentation.utils.Strings

class SignOutUseCase(
    private val userRepository: UserRepository,
    private val scheduleNotificationService: ScheduleNotificationService,
    private val strings: Strings
) {
    sealed class Result {
        object Success : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(): Result {
        return try {
            // Cancelar todas las notificaciones programadas
            scheduleNotificationService.cancelAllNotifications()
            
            // Cerrar sesión
            userRepository.signOut()
            
            // Verificar que la sesión se cerró correctamente
            if (userRepository.getFirebaseUser() == null) {
                Result.Success
            } else {
                Result.Error(strings.getSomethingWentWrong())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: strings.getUnknownError())
        }
    }
} 