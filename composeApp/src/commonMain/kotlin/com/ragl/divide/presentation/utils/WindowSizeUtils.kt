package com.ragl.divide.presentation.utils

import androidx.compose.runtime.Composable

// Enum común para representar los tamaños de ventana
enum class WindowWidthSizeClass {
    Compact, Medium, Expanded
}

// Función expect para obtener el tamaño de ventana según la plataforma
@Composable
expect fun getWindowWidthSizeClass(): WindowWidthSizeClass 