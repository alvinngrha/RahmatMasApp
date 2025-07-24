package com.example.rahmatmas.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.rahmatmas.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AdminDataStore(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("admin_prefs")

        // Keys untuk DataStore
        private val IS_ADMIN_LOGGED_IN = booleanPreferencesKey("is_admin_logged_in")
        private val ADMIN_USERNAME = stringPreferencesKey("admin_username")
        private val LOGIN_TIMESTAMP = stringPreferencesKey("login_timestamp")

        // Session duration: 24 jam
        private const val SESSION_DURATION = 24 * 60 * 60 * 1000L
    }

    // Simpan status login admin
    suspend fun saveAdminLoginStatus(isLoggedIn: Boolean, username: String = "") {
        context.dataStore.edit { preferences ->
            preferences[IS_ADMIN_LOGGED_IN] = isLoggedIn
            preferences[ADMIN_USERNAME] = username
            if (isLoggedIn) {
                preferences[LOGIN_TIMESTAMP] = System.currentTimeMillis().toString()
            } else {
                preferences.remove(LOGIN_TIMESTAMP)
            }
        }
    }

    // Baca status login admin
    val isAdminLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_ADMIN_LOGGED_IN] ?: false
    }

    // Baca username admin
    val adminUsername: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ADMIN_USERNAME] ?: ""
    }

    // Fungsi untuk validasi login admin
    fun validateAdminCredentials(username: String, password: String): Boolean {
        return username == BuildConfig.ADMIN_USERNAME && password == BuildConfig.ADMIN_PASSWORD
    }

    // Logout admin
    suspend fun logoutAdmin() {
        context.dataStore.edit { preferences ->
            preferences[IS_ADMIN_LOGGED_IN] = false
            preferences[ADMIN_USERNAME] = ""
            preferences.remove(LOGIN_TIMESTAMP) // Hapus timestamp saat logout
        }
    }

    // Cek apakah session masih valid (opsional, misal 24 jam)
    val isSessionValid: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val isLoggedIn = preferences[IS_ADMIN_LOGGED_IN] ?: false
        val timestamp = preferences[LOGIN_TIMESTAMP]?.toLongOrNull() ?: 0L
        val currentTime = System.currentTimeMillis()

        // Return true jika:
        // 1. User sudah login DAN
        // 2. Timestamp ada (bukan 0) DAN
        // 3. Waktu sekarang masih dalam batas session duration
        isLoggedIn && timestamp > 0L && (currentTime - timestamp) < SESSION_DURATION
    }
}