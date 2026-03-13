package com.panchangam100.live.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.panchangam100.live.data.model.AppLocation
import com.panchangam100.live.data.model.Language
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("panchangam100_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store = context.dataStore

    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_LOC_NAME = stringPreferencesKey("loc_name")
        val KEY_LOC_STATE = stringPreferencesKey("loc_state")
        val KEY_LOC_LAT = doublePreferencesKey("loc_lat")
        val KEY_LOC_LON = doublePreferencesKey("loc_lon")
        val KEY_LOC_TZ = stringPreferencesKey("loc_tz")
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEY_USE_24H = booleanPreferencesKey("use_24h")

        // Default: Tirupati
        val DEFAULT_LOCATION = AppLocation(
            name = "Tirupati",
            stateName = "Andhra Pradesh",
            latitude = 13.6288,
            longitude = 79.4192,
            timezone = "Asia/Kolkata"
        )
    }

    val languageFlow: Flow<Language> = store.data.map { prefs ->
        Language.fromCode(prefs[KEY_LANGUAGE] ?: Language.TELUGU.code)
    }

    val locationFlow: Flow<AppLocation> = store.data.map { prefs ->
        val name = prefs[KEY_LOC_NAME]
        if (name != null) {
            AppLocation(
                name = name,
                stateName = prefs[KEY_LOC_STATE] ?: "",
                latitude = prefs[KEY_LOC_LAT] ?: DEFAULT_LOCATION.latitude,
                longitude = prefs[KEY_LOC_LON] ?: DEFAULT_LOCATION.longitude,
                timezone = prefs[KEY_LOC_TZ] ?: DEFAULT_LOCATION.timezone
            )
        } else DEFAULT_LOCATION
    }

    val darkModeFlow: Flow<Boolean?> = store.data.map { prefs ->
        prefs[KEY_DARK_MODE]
    }

    val use24hFlow: Flow<Boolean> = store.data.map { prefs ->
        prefs[KEY_USE_24H] ?: false
    }

    suspend fun setLanguage(lang: Language) {
        store.edit { it[KEY_LANGUAGE] = lang.code }
    }

    suspend fun setLocation(loc: AppLocation) {
        store.edit { prefs ->
            prefs[KEY_LOC_NAME] = loc.name
            prefs[KEY_LOC_STATE] = loc.stateName
            prefs[KEY_LOC_LAT] = loc.latitude
            prefs[KEY_LOC_LON] = loc.longitude
            prefs[KEY_LOC_TZ] = loc.timezone
        }
    }

    suspend fun setDarkMode(dark: Boolean?) {
        store.edit { prefs ->
            if (dark == null) prefs.remove(KEY_DARK_MODE)
            else prefs[KEY_DARK_MODE] = dark
        }
    }

    suspend fun setUse24h(use: Boolean) {
        store.edit { it[KEY_USE_24H] = use }
    }
}
