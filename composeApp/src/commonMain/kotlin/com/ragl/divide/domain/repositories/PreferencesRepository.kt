package com.ragl.divide.domain.repositories

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val darkModeFlow: Flow<String?>
    val isFirstTimeFlow: Flow<Boolean>
    suspend fun saveDarkMode(darkMode: Boolean?): Boolean
    suspend fun setFirstTime(isFirstTime: Boolean): Boolean
}
