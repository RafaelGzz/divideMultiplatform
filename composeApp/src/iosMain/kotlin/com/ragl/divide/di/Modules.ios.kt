package com.ragl.divide.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ragl.divide.data.services.ScheduleNotificationService
import com.ragl.divide.ui.utils.NotificationStrings
import createDataStore
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {
    single {
        createDataStore()
    }.bind<DataStore<Preferences>>()

    single{
        ScheduleNotificationService()
    }.bind<ScheduleNotificationService>()

    single{
        NotificationStrings()
    }.bind<NotificationStrings>()
}