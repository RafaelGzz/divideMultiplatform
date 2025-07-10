package com.ragl.divide

import androidx.compose.ui.window.ComposeUIViewController
import com.ragl.divide.di.initKoin
import com.ragl.divide.presentation.screens.App

fun MainViewController() = ComposeUIViewController(configure = {
    initKoin()
},) {
    App()
}