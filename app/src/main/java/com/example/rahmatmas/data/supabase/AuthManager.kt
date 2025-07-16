package com.example.rahmatmas.data.supabase

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthManager {
    private val supabaseClient = SupabaseModule.client

    // Flow untuk memantau status login
    val isUserLoggedIn: Flow<Boolean> = flow {
        emit(supabaseClient.auth.currentUserOrNull() != null)
    }

    // Mendapatkan user yang sedang login
    fun getCurrentUser(): UserInfo? {
        return supabaseClient.auth.currentUserOrNull()
    }

    // Login dengan Google
    suspend fun signInGoogle(context: Context): AuthResponse {
        return try {
            val credentialManager = CredentialManager.create(context)

            // Konfigurasi Google ID Option
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("426296330523-f7h31tm1o4ip4hjamhnspa8vij8t18is.apps.googleusercontent.com") // Ganti dengan Web Client ID dari Google Cloud Console
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Mendapatkan credential dari Google
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            // Parse Google ID Token
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            // Sign in ke Supabase dengan Google ID Token
            supabaseClient.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }

            AuthResponse.Success

        } catch (e: GetCredentialException) {
            Log.e("AuthManager", "Error getting credential", e)
            AuthResponse.Error("Login gagal: ${e.message}")
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("AuthManager", "Error parsing Google ID token", e)
            AuthResponse.Error("Error parsing token: ${e.message}")
        } catch (e: Exception) {
            Log.e("AuthManager", "Unexpected error during sign in", e)
            AuthResponse.Error("Login gagal: ${e.message}")
        }
    }

    // Logout
    suspend fun signOut(): AuthResponse {
        return try {
            supabaseClient.auth.signOut()
            AuthResponse.Success
        } catch (e: Exception) {
            Log.e("AuthManager", "Sign out failed", e)
            AuthResponse.Error("Logout gagal: ${e.message}")
        }
    }

    // Check apakah user sudah login
    fun isLoggedIn(): Boolean {
        return supabaseClient.auth.currentUserOrNull() != null
    }
}