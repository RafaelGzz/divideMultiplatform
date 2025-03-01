package com.ragl.divide

import androidx.compose.ui.window.ComposeUIViewController
import com.ragl.divide.di.initKoin

fun MainViewController() = ComposeUIViewController(configure = {
    initKoin()
}) { App() }