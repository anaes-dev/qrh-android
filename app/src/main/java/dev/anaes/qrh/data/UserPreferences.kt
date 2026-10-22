package dev.anaes.qrh.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.anaes.qrh.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** SharedPreferences file used by the pre-Compose app (v2.x). */
private const val LEGACY_PREFS_NAME = "dev.anaes.qrh"

/** SharedPreferences file (and key) used before the package rename from com.mttrnd.qrh. */
private const val LEGACY_OLD_PACKAGE_PREFS_NAME = "com.mttrnd.qrh.seenwarning"

/**
 * One-off migrations from the pre-Compose SharedPreferences into DataStore. Keys keep the same
 * names, so values carry straight across; existing DataStore values are never overwritten and
 * the old files are cleared once copied.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "qrh_prefs",
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(context, LEGACY_PREFS_NAME),
            SharedPreferencesMigration(
                context,
                LEGACY_OLD_PACKAGE_PREFS_NAME,
                keysToMigrate = setOf(LEGACY_OLD_PACKAGE_PREFS_NAME),
            ),
        )
    },
)

class UserPreferences(private val context: Context) {

    private val nightDisabledKey = booleanPreferencesKey("night_disabled")
    private val expandingDisabledKey = booleanPreferencesKey("expanding_disabled")
    private val seenWarningKey = booleanPreferencesKey("seen_warning")
    private val legacySeenWarningKey = booleanPreferencesKey(LEGACY_OLD_PACKAGE_PREFS_NAME)
    private val versionKey = intPreferencesKey("version")

    val nightDisabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[nightDisabledKey] ?: false
    }

    val expandingDisabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[expandingDisabledKey] ?: false
    }

    val disclaimerStatus: Flow<DisclaimerStatus> = context.dataStore.data.map { prefs ->
        disclaimerStatusFor(
            seenWarning = prefs[seenWarningKey] ?: false,
            legacySeenWarning = prefs[legacySeenWarningKey] ?: false,
            savedVersion = prefs[versionKey] ?: 0,
            currentVersion = BuildConfig.VERSION_CODE,
        )
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
            prefs.remove(legacySeenWarningKey)
        }
    }
}
