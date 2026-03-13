package dev.anaes.qrh.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.anaes.qrh.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qrh_prefs")

class UserPreferences(private val context: Context) {

    private val nightDisabledKey = booleanPreferencesKey("night_disabled")
    private val expandingDisabledKey = booleanPreferencesKey("expanding_disabled")
    private val seenWarningKey = booleanPreferencesKey("seen_warning")
    private val versionKey = intPreferencesKey("version")

    val nightDisabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[nightDisabledKey] ?: false
    }

    val expandingDisabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[expandingDisabledKey] ?: false
    }

    val disclaimersAccepted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        val seen = prefs[seenWarningKey] ?: false
        val savedVersion = prefs[versionKey] ?: 0
        seen && savedVersion >= BuildConfig.VERSION_CODE
    }

    suspend fun setNightDisabled(disabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[nightDisabledKey] = disabled
        }
    }

    suspend fun setExpandingDisabled(disabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[expandingDisabledKey] = disabled
        }
    }

    suspend fun acceptDisclaimers() {
        context.dataStore.edit { prefs ->
            prefs[seenWarningKey] = true
            prefs[versionKey] = BuildConfig.VERSION_CODE
        }
    }

    suspend fun migrateFromSharedPrefs(context: Context) {
        val oldPrefs = context.getSharedPreferences("dev.anaes.qrh", Context.MODE_PRIVATE)
        if (oldPrefs.contains("seen_warning") || oldPrefs.contains("version")) {
            context.dataStore.edit { prefs ->
                if (oldPrefs.contains("seen_warning")) {
                    prefs[seenWarningKey] = oldPrefs.getBoolean("seen_warning", false)
                }
                if (oldPrefs.contains("version")) {
                    prefs[versionKey] = oldPrefs.getInt("version", 0)
                }
                if (oldPrefs.contains("night_disabled")) {
                    prefs[nightDisabledKey] = oldPrefs.getBoolean("night_disabled", false)
                }
                if (oldPrefs.contains("expanding_disabled")) {
                    prefs[expandingDisabledKey] = oldPrefs.getBoolean("expanding_disabled", false)
                }
            }
            oldPrefs.edit().clear().apply()
        }
    }
}
