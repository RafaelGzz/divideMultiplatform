package com.ragl.divide.di

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ragl.divide.AndroidAppLifecycleHandler
import com.ragl.divide.AppLifecycleHandler
import com.ragl.divide.createDataStore
import com.ragl.divide.data.services.ScheduleNotificationServiceImpl
import com.ragl.divide.domain.services.ScheduleNotificationService
import com.ragl.divide.presentation.utils.Strings
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule: Module = module {
    single {
        createDataStore(context = get())
    }.bind<DataStore<Preferences>>()

    single{
        ScheduleNotificationServiceImpl(context = get())
    }.bind<ScheduleNotificationService>()

    single{
        Strings(context = get())
    }.bind<Strings>()

    single<AppLifecycleHandler> {
        AndroidAppLifecycleHandler(get<Application>())
    }
}