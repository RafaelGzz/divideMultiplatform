package com.ragl.divide.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ragl.divide.createDataStore
import com.ragl.divide.data.services.ScheduleNotificationService
import com.ragl.divide.ui.utils.NotificationStrings
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module{
    single {
        createDataStore(context = get())
    }.bind<DataStore<Preferences>>()

    single{
        ScheduleNotificationService(context = get())
    }.bind<ScheduleNotificationService>()

    single{
        NotificationStrings(context = get())
    }.bind<NotificationStrings>()
}