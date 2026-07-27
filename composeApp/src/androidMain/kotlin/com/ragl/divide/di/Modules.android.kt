package com.ragl.divide.di

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ragl.divide.data.createDataStore
import com.ragl.divide.data.services.ScheduleNotificationServiceImpl
import com.ragl.divide.domain.AndroidAppLifecycleHandler
import com.ragl.divide.domain.AppLifecycleHandler
import com.ragl.divide.domain.services.ScheduleNotificationService
import com.ragl.divide.presentation.utils.Strings
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule: Module = module {
    single {
        com.ragl.divide.data.createDataStore(context = get())
    }.bind<DataStore<Preferences>>()

    single{
        com.ragl.divide.data.services.ScheduleNotificationServiceImpl(context = get())
    }.bind<ScheduleNotificationService>()

    single{
        com.ragl.divide.presentation.utils.Strings(context = get())
    }.bind<com.ragl.divide.presentation.utils.Strings>()

    single<AppLifecycleHandler> {
        com.ragl.divide.domain.AndroidAppLifecycleHandler(get<Application>())
    }
}