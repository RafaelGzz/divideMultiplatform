package com.ragl.divide.data.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ragl.divide.data.repositories.UserRepository
import com.ragl.divide.ui.utils.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class BootReminderNotificationsReceiver : BroadcastReceiver(), KoinComponent {
    // Inyectar userRepository usando Koin
    private val userRepository: UserRepository by inject()
    private val strings: Strings by inject()

    override fun onReceive(context: Context, intent: Intent) = goAsync {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scheduleNotificationService = ScheduleNotificationService(context)
            userRepository.getExpenses().values.forEach {
                if(it.reminders && !it.paid)
                    scheduleNotificationService.scheduleNotification(
                        id = it.id.takeLast(5).toInt(),
                        title = strings.getNotificationTitleString(it.title),
                        message = strings.getNotificationBodyString(),
                        it.startingDate,
                        it.frequency
                    )
            }
        }
    }
}

fun BroadcastReceiver.goAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) {
    val pendingResult = goAsync()
    @OptIn(DelicateCoroutinesApi::class) // Must run globally; there's no teardown callback.
    GlobalScope.launch(context) {
        try {
            block()
        } finally {
            pendingResult.finish()
        }
    }
}