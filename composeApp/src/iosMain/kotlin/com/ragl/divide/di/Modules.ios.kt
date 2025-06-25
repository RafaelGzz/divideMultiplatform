package com.ragl.divide.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ragl.divide.AppLifecycleHandler
import com.ragl.divide.IOSAppLifecycleHandler
import com.ragl.divide.data.services.ScheduleNotificationService
import com.ragl.divide.ui.utils.Strings
import createDataStore
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
        Strings()
    }.bind<Strings>()

    single<AppLifecycleHandler> {
        IOSAppLifecycleHandler()
    }
}