package com.ragl.divide

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.ragl.divide.di.initKoin
import createDataStore

fun MainViewController() = ComposeUIViewController(configure = {
    initKoin()
},) {
    App()
}