package com.ragl.divide.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ragl.divide.domain.repositories.PreferencesRepository
import com.ragl.divide.presentation.utils.logMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class PreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {
    private companion object Companion {
        val KEY_DARK_MODE = stringPreferencesKey("dark_mode")
        val KEY_IS_FIRST_TIME = booleanPreferencesKey("is_first_time")
    }

    override val darkModeFlow: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) {
                logMessage("PreferencesRepository", "Error reading preferences: $it")
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {
            it[KEY_DARK_MODE]
        }

    override val isFirstTimeFlow: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                logMessage("PreferencesRepository", "Error reading preferences: $it")
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {
            it[KEY_IS_FIRST_TIME] != false // Verdadero por defecto para nuevas instalaciones
        }

    override suspend fun saveDarkMode(darkMode: Boolean?): Boolean =
        try {
            dataStore.edit {
                if (darkMode == null) it.remove(KEY_DARK_MODE)
                else it[KEY_DARK_MODE] = darkMode.toString()
            }
            true
        } catch (e: Exception) {
            logMessage("PreferencesRepository", "Error saving start destination: $e")
            false
        }

    override suspend fun setFirstTime(isFirstTime: Boolean): Boolean =
        try {
            dataStore.edit {
                it[KEY_IS_FIRST_TIME] = isFirstTime
            }
            true
        } catch (e: Exception) {
            logMessage("PreferencesRepository", "Error saving first time completed: $e")
            false
        }
}