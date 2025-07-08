package com.ragl.divide.presentation.state

import androidx.compose.runtime.compositionLocalOf
import com.ragl.divide.domain.stateHolders.UserState

val LocalUserState = compositionLocalOf<UserState>{ UserState() }
val LocalThemeState = compositionLocalOf<String?> { null }