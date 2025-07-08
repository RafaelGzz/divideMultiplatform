package com.ragl.divide.data.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ragl.divide.data.models.Frequency
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.ScheduleNotificationService
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.logMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class BootReminderNotificationsReceiver : BroadcastReceiver(), KoinComponent {
    private val userRepository: UserRepository by inject()
    private val strings: Strings by inject()
    private val logTag = "BootReminderReceiver"

    @OptIn(ExperimentalTime::class)
    override fun onReceive(context: Context, intent: Intent) = goAsync {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scheduleNotificationService by inject<ScheduleNotificationService>()

            logMessage(logTag, "Dispositivo reiniciado, reprogramando notificaciones")
            val currentUser = userRepository.getFirebaseUser()
            if (currentUser == null) return@goAsync
            val user = userRepository.getUser(currentUser.uid)
            val expenses = user.expenses

            if (expenses.isEmpty()) {
                logMessage(logTag, "No hay gastos para programar")
                return@goAsync
            }

            expenses.values.forEach { expense ->
                if (expense.reminders && !expense.paid) {
                    try {
                        val notificationId =
                            expense.id.takeLast(5).toIntOrNull() ?: expense.hashCode()

                        val now = Clock.System.now().toEpochMilliseconds()
                        if (expense.startingDate >= now || expense.frequency != Frequency.ONCE) {
                            scheduleNotificationService.cancelNotification(notificationId)
                            scheduleNotificationService.scheduleNotification(
                                id = notificationId,
                                title = strings.getAppName(),
                                message = strings.getNotificationBodyString(expense.title),
                                expense.startingDate,
                                expense.frequency,
                                true
                            )
                            logMessage(
                                logTag,
                                "${expense.title} reprogramada cada ${expense.frequency} desde ${expense.startingDate}"
                            )
                        }
                    } catch (e: Exception) {
                        logMessage(logTag, "Error al programar notificación: ${e.message}")
                    }
                }
            }
        }
    }
}

fun BroadcastReceiver.goAsync(
    context: CoroutineContext = Dispatchers.IO,
    block: suspend CoroutineScope.() -> Unit
) {
    val pendingResult = goAsync()
    @OptIn(DelicateCoroutinesApi::class)
    GlobalScope.launch(context) {
        try {
            block()
        } catch (e: Exception) {
            // Capturar cualquier excepción para evitar que el BroadcastReceiver falle silenciosamente
            e.printStackTrace()
        } finally {
            pendingResult.finish()
        }
    }
}