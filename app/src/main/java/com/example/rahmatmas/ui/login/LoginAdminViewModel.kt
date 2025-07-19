package com.example.rahmatmas.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.datastore.AdminAuthManager
import com.example.rahmatmas.data.supabase.AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginAdminViewModel(
    private val adminAuthManager: AdminAuthManager
) : ViewModel() {

    // State untuk login admin
    private val _loginState = MutableStateFlow<AuthResponse?>(null)
    val loginState: StateFlow<AuthResponse?> = _loginState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    // Fungsi untuk mengupdate username
    fun updateUsername(newUsername: String) {
        _username.value = newUsername
    }

    // Fungsi untuk mengupdate password
    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    // Fungsi login admin
    fun loginAdmin() {
        viewModelScope.launch {
            _isLoading.value = true
            _loginState.value = null

            val result = adminAuthManager.loginAdmin(
                username = _username.value,
                password = _password.value
            )

            _loginState.value = result
            _isLoading.value = false
        }
    }

    // Fungsi logout admin
    fun logoutAdmin() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = adminAuthManager.logoutAdmin()
            _loginState.value = result
            _isLoading.value = false
        }
    }

    // Cek apakah admin sudah login
    fun isAdminLoggedIn() = adminAuthManager.isAdminLoggedIn()

    // Dapatkan username admin
    fun getAdminUsername() = adminAuthManager.getAdminUsername()

    // Reset state
    fun resetLoginState() {
        _loginState.value = null
    }

    // Clear form
    fun clearForm() {
        _username.value = ""
        _password.value = ""
    }
}