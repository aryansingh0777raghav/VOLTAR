package com.voltar.battery.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "voltar_preferences")

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

data class UserPreferences(
    val themeMode: ThemeMode,
    val isMonitoringEnabled: Boolean,
    val collectionIntervalMinutes: Int,
    val customDesignCapacityMah: Float?,
    val firstLaunchTimestamp: Long,
    val isOnboardingCompleted: Boolean
)

class UserPreferencesRepository(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_MONITORING_ENABLED = booleanPreferencesKey("monitoring_enabled")
        private val KEY_COLLECTION_INTERVAL = intPreferencesKey("collection_interval_minutes")
        private val KEY_CUSTOM_CAPACITY = floatPreferencesKey("custom_design_capacity")
        private val KEY_FIRST_LAUNCH = longPreferencesKey("first_launch_timestamp")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                emit(emptyPreferences())
            }
        }
        .map { preferences ->
        val themeString = preferences[KEY_THEME_MODE] ?: ThemeMode.LIGHT.name
        val theme = try {
            ThemeMode.valueOf(themeString)
        } catch (_: Exception) {
            ThemeMode.LIGHT
        }
        val monitoring = preferences[KEY_MONITORING_ENABLED] ?: true
        val interval = preferences[KEY_COLLECTION_INTERVAL] ?: 15
        val customCap = preferences[KEY_CUSTOM_CAPACITY]
        val firstLaunch = preferences[KEY_FIRST_LAUNCH] ?: System.currentTimeMillis()
        val onboarding = preferences[KEY_ONBOARDING_COMPLETED] ?: false

        UserPreferences(
            themeMode = theme,
            isMonitoringEnabled = monitoring,
            collectionIntervalMinutes = interval,
            customDesignCapacityMah = customCap,
            firstLaunchTimestamp = firstLaunch,
            isOnboardingCompleted = onboarding
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setMonitoringEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_MONITORING_ENABLED] = enabled
        }
    }

    suspend fun setCollectionInterval(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_COLLECTION_INTERVAL] = minutes
        }
    }

    suspend fun setCustomDesignCapacity(capacityMah: Float?) {
        dataStore.edit { preferences ->
            if (capacityMah != null && capacityMah > 0) {
                preferences[KEY_CUSTOM_CAPACITY] = capacityMah
            } else {
                preferences.remove(KEY_CUSTOM_CAPACITY)
            }
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun initFirstLaunchIfEmpty() {
        dataStore.edit { preferences ->
            if (preferences[KEY_FIRST_LAUNCH] == null) {
                preferences[KEY_FIRST_LAUNCH] = System.currentTimeMillis()
            }
        }
    }

    suspend fun resetAll() {
        dataStore.edit { preferences ->
            preferences.clear()
            preferences[KEY_THEME_MODE] = ThemeMode.LIGHT.name
            preferences[KEY_FIRST_LAUNCH] = System.currentTimeMillis()
            preferences[KEY_ONBOARDING_COMPLETED] = false
        }
    }
}
