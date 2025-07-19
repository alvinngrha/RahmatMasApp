package com.example.rahmatmas.data.datastore

import android.content.Context
import com.example.rahmatmas.data.supabase.AuthResponse
import kotlinx.coroutines.flow.Flow

class AdminAuthManager(context: Context) {

    private val adminDataStore = AdminDataStore(context)

    // Login admin
    suspend fun loginAdmin(username: String, password: String): AuthResponse {
        return try {
            if (username.isBlank() || password.isBlank()) {
                AuthResponse.Error("Username dan password tidak boleh kosong")
            } else {
                val isValid = adminDataStore.validateAdminCredentials(username, password)
                if (isValid) {
                    adminDataStore.saveAdminLoginStatus(true, username)
                    AuthResponse.Success
                } else {
                    AuthResponse.Error("Username atau password salah")
                }
            }
        } catch (e: Exception) {
            AuthResponse.Error("Terjadi kesalahan: ${e.message}")
        }
    }

    // Logout admin
    suspend fun logoutAdmin(): AuthResponse {
        return try {
            adminDataStore.logoutAdmin()
            AuthResponse.Success
        } catch (e: Exception) {
            AuthResponse.Error("Gagal logout: ${e.message}")
        } finally {
            // Clear any sensitive data
            adminDataStore.saveAdminLoginStatus(false, "")
        }
    }

    // Cek status login
    fun isAdminLoggedIn(): Flow<Boolean> = adminDataStore.isAdminLoggedIn

    // Dapatkan username admin
    fun getAdminUsername(): Flow<String> = adminDataStore.adminUsername

    // Cek session validity
    fun isSessionValid(): Flow<Boolean> = adminDataStore.isSessionValid
}