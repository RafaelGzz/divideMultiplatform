package com.ragl.divide.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIScreen

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun getWindowWidthSizeClass(): WindowWidthSizeClass {
    val density = LocalDensity.current
    
    // Obtener el ancho de pantalla en dp
    val screenWidthPx = UIScreen.mainScreen.bounds.useContents { size.width }
    val screenWidthDp = with(density) { screenWidthPx.dp }
    
    return when {
        screenWidthDp < 600.dp -> WindowWidthSizeClass.Compact    // Pantallas pequeñas (teléfonos)
        screenWidthDp < 840.dp -> WindowWidthSizeClass.Medium     // Pantallas medianas (tablets pequeños)
        else -> WindowWidthSizeClass.Expanded                     // Pantallas grandes (tablets grandes)
    }
} 