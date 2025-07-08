package com.ragl.divide.presentation.utils

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass as MaterialWindowWidthSizeClass

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
actual fun getWindowWidthSizeClass(): WindowWidthSizeClass {
    val context = LocalContext.current
    val activity = context as androidx.activity.ComponentActivity
    val windowSizeClass = calculateWindowSizeClass(activity)
    
    return when (windowSizeClass.widthSizeClass) {
        MaterialWindowWidthSizeClass.Compact -> WindowWidthSizeClass.Compact
        MaterialWindowWidthSizeClass.Medium -> WindowWidthSizeClass.Medium
        MaterialWindowWidthSizeClass.Expanded -> WindowWidthSizeClass.Expanded
        else -> WindowWidthSizeClass.Expanded
    }
} 