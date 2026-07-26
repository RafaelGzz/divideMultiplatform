package com.ragl.divide.presentation

import android.app.Application
import com.ragl.divide.di.initKoin
import org.koin.android.ext.koin.androidContext

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin{
            androidContext(applicationContext)
        }
    }
}