package com.ragl.divide

import android.app.Application
import com.ragl.divide.di.initKoin

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}