package com.ragl.divide.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ragl.divide.ui.utils.logMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class PreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val KEY_START_DESTINATION = stringPreferencesKey("start_destination")
        val KEY_DARK_MODE = stringPreferencesKey("dark_mode")
    }

    val startDestinationFlow: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                logMessage("PreferencesRepository","Error reading preferences: $it")
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {
            it[KEY_START_DESTINATION] ?: "Login"
        }

    val darkModeFlow: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) {
                logMessage("PreferencesRepository","Error reading preferences: $it")
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {
            it[KEY_DARK_MODE]
        }

    suspend fun saveStartDestination(startDestination: String): Boolean =
        try {
            dataStore.edit {
                it[KEY_START_DESTINATION] = startDestination
            }
            true
        } catch (e: Exception) {
            logMessage("PreferencesRepository","Error saving start destination: $e")
            false
        }


    suspend fun saveDarkMode(darkMode: Boolean?): Boolean =
        try {
            dataStore.edit {
                it[KEY_DARK_MODE] = darkMode.toString()
            }
            true
        } catch (e: Exception) {
            logMessage("PreferencesRepository","Error saving start destination: $e")
            false
        }
}