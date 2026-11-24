package com.example.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.DisplayLanguage
import com.example.model.KuralSolution
import com.example.model.RecentSearchEntry
import com.example.model.VoiceLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import java.io.IOException

private val Context.kuralDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "thirukkural_local_storage"
)

private const val TAG = "LocalStorageRepo"
private const val MAX_RECENT_SEARCHES = 5
private const val SYNC_PREFS_NAME = "thirukkural_session_sync_prefs"
private const val SYNC_KEY_GUEST_MODE = "is_guest_mode"
private const val SYNC_KEY_DARK_MODE = "dark_mode_enabled"

class LocalStorageRepository(context: Context) {

    private val appContext = context.applicationContext
    private val dataStore = appContext.kuralDataStore
    private val syncPrefs = appContext.getSharedPreferences(SYNC_PREFS_NAME, Context.MODE_PRIVATE)

    private object Keys {
        val RECENT_SEARCHES_JSON = stringPreferencesKey("recent_searches_json")
        val GUEST_SAVED_KURALS_JSON = stringPreferencesKey("guest_saved_kurals_json")
        val DISPLAY_LANGUAGE = stringPreferencesKey("display_language")
        val VOICE_LANGUAGE = stringPreferencesKey("voice_language")
        val IS_GUEST_MODE = booleanPreferencesKey("is_guest_mode")
        val GUEST_DISPLAY_NAME = stringPreferencesKey("guest_display_name")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val PROFILE_PHOTO_PATH = stringPreferencesKey("profile_photo_path")
    }

    private val safePreferences: Flow<Preferences> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            Log.e(TAG, "Error reading DataStore preferences", exception)
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }

    val recentSearchesFlow: Flow<List<RecentSearchEntry>> = safePreferences.map { prefs ->
        parseRecentSearches(prefs[Keys.RECENT_SEARCHES_JSON])
    }

    val guestSavedKuralsFlow: Flow<List<KuralSolution>> = safePreferences.map { prefs ->
        parseSavedKurals(prefs[Keys.GUEST_SAVED_KURALS_JSON])
    }

    val displayLanguageFlow: Flow<DisplayLanguage> = safePreferences.map { prefs ->
        DisplayLanguage.fromRaw(prefs[Keys.DISPLAY_LANGUAGE])
    }

    val voiceLanguageFlow: Flow<VoiceLanguage> = safePreferences.map { prefs ->
        VoiceLanguage.fromRaw(prefs[Keys.VOICE_LANGUAGE])
    }

    val isGuestModeFlow: Flow<Boolean> = safePreferences.map { prefs ->
        val value = prefs[Keys.IS_GUEST_MODE] ?: syncPrefs.getBoolean(SYNC_KEY_GUEST_MODE, false)
        if (value && !syncPrefs.getBoolean(SYNC_KEY_GUEST_MODE, false)) {
            syncPrefs.edit().putBoolean(SYNC_KEY_GUEST_MODE, true).apply()
        }
        value
    }

    val guestDisplayNameFlow: Flow<String> = safePreferences.map { prefs ->
        prefs[Keys.GUEST_DISPLAY_NAME]?.takeIf { it.isNotBlank() } ?: "Guest Seeker"
    }

    val isDarkModeFlow: Flow<Boolean> = safePreferences.map { prefs ->
        val value = prefs[Keys.DARK_MODE_ENABLED] ?: syncPrefs.getBoolean(SYNC_KEY_DARK_MODE, false)
        if (value != syncPrefs.getBoolean(SYNC_KEY_DARK_MODE, false)) {
            syncPrefs.edit().putBoolean(SYNC_KEY_DARK_MODE, value).apply()
        }
        value
    }

    val profilePhotoPathFlow: Flow<String?> = safePreferences.map { prefs ->
        prefs[Keys.PROFILE_PHOTO_PATH]?.takeIf { it.isNotBlank() }
    }

    fun getInitialGuestModeSync(): Boolean =
        syncPrefs.getBoolean(SYNC_KEY_GUEST_MODE, false)

    fun getInitialDarkModeSync(): Boolean =
        syncPrefs.getBoolean(SYNC_KEY_DARK_MODE, false)

    suspend fun addRecentSearch(query: String, solution: KuralSolution) {
        val cleanedQuery = query.trim()
        if (cleanedQuery.isBlank()) return

        dataStore.edit { prefs ->
            val existing = parseRecentSearches(prefs[Keys.RECENT_SEARCHES_JSON])
            val normalizedNew = cleanedQuery.lowercase()
            val deduped = existing.filterNot { it.query.trim().lowercase() == normalizedNew }
            val updated = buildList {
                add(
                    RecentSearchEntry(
                        query = cleanedQuery,
                        timestampMillis = System.currentTimeMillis(),
                        solution = solution.copy(query = cleanedQuery)
                    )
                )
                addAll(deduped)
            }.take(MAX_RECENT_SEARCHES)

            val jsonArray = JSONArray()
            updated.forEach { jsonArray.put(it.toJson()) }
            prefs[Keys.RECENT_SEARCHES_JSON] = jsonArray.toString()
        }
    }

    suspend fun clearRecentSearches() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.RECENT_SEARCHES_JSON)
        }
    }

    suspend fun saveGuestKural(solution: KuralSolution) {
        dataStore.edit { prefs ->
            val existing = parseSavedKurals(prefs[Keys.GUEST_SAVED_KURALS_JSON])
            val filtered = existing.filterNot { it.kuralNumber == solution.kuralNumber }
            val updated = buildList {
                add(solution.copy(docId = solution.stableDocId, createdAtMillis = System.currentTimeMillis()))
                addAll(filtered)
            }
            val jsonArray = JSONArray()
            updated.forEach { jsonArray.put(it.toJson()) }
            prefs[Keys.GUEST_SAVED_KURALS_JSON] = jsonArray.toString()
        }
    }

    suspend fun removeGuestKural(kuralNumber: Int) {
        dataStore.edit { prefs ->
            val existing = parseSavedKurals(prefs[Keys.GUEST_SAVED_KURALS_JSON])
            val updated = existing.filterNot { it.kuralNumber == kuralNumber }
            val jsonArray = JSONArray()
            updated.forEach { jsonArray.put(it.toJson()) }
            prefs[Keys.GUEST_SAVED_KURALS_JSON] = jsonArray.toString()
        }
    }

    suspend fun setDisplayLanguage(language: DisplayLanguage) {
        dataStore.edit { prefs ->
            prefs[Keys.DISPLAY_LANGUAGE] = language.name
        }
    }

    suspend fun setVoiceLanguage(language: VoiceLanguage) {
        dataStore.edit { prefs ->
            prefs[Keys.VOICE_LANGUAGE] = language.name
        }
    }

    suspend fun setGuestMode(active: Boolean) {
        syncPrefs.edit().putBoolean(SYNC_KEY_GUEST_MODE, active).apply()
        dataStore.edit { prefs ->
            prefs[Keys.IS_GUEST_MODE] = active
        }
    }

    suspend fun setGuestDisplayName(name: String) {
        val trimmed = name.trim().take(60)
        dataStore.edit { prefs ->
            if (trimmed.isNotBlank()) {
                prefs[Keys.GUEST_DISPLAY_NAME] = trimmed
            }
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        syncPrefs.edit().putBoolean(SYNC_KEY_DARK_MODE, enabled).apply()
        dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE_ENABLED] = enabled
        }
    }

    suspend fun setProfilePhotoPath(path: String?) {
        dataStore.edit { prefs ->
            if (path.isNullOrBlank()) {
                prefs.remove(Keys.PROFILE_PHOTO_PATH)
            } else {
                prefs[Keys.PROFILE_PHOTO_PATH] = path
            }
        }
    }

    private fun parseRecentSearches(rawJson: String?): List<RecentSearchEntry> {
        if (rawJson.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(rawJson)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    RecentSearchEntry.fromJson(obj)?.let { add(it) }
                }
            }.take(MAX_RECENT_SEARCHES)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse recent searches JSON", e)
            emptyList()
        }
    }

    private fun parseSavedKurals(rawJson: String?): List<KuralSolution> {
        if (rawJson.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(rawJson)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    add(KuralSolution.fromJson(obj))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse guest saved kurals JSON", e)
            emptyList()
        }
    }
}
