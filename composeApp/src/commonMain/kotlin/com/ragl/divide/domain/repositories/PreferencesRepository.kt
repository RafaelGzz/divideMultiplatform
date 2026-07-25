package com.ragl.divide.domain.repositories

import io.mockative.Mockable
import kotlinx.coroutines.flow.Flow

@Mockable
interface PreferencesRepository {
    val darkModeFlow: Flow<String?>
    val isFirstTimeFlow: Flow<Boolean>
    suspend fun saveDarkMode(darkMode: Boolean?): Boolean
    suspend fun setFirstTime(isFirstTime: Boolean): Boolean
}
