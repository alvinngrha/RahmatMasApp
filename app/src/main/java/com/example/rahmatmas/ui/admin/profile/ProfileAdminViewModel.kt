package com.example.rahmatmas.ui.admin.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.datastore.AdminAuthManager
import com.example.rahmatmas.data.supabase.AuthResponse
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileAdminViewModel(
    private val adminAuthManager: AdminAuthManager
) : ViewModel() {

    // Expose username as StateFlow for Compose
    val adminUsernameFlow: StateFlow<String> =
        adminAuthManager.getAdminUsername()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    // Helper getter for current value
    val adminUsername: String
        get() = adminUsernameFlow.value

    // Expose session validity
    val isSessionValidFlow: StateFlow<Boolean> =
        adminAuthManager.isSessionValid()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isSessionValid: Boolean
        get() = isSessionValidFlow.value

    fun logoutAdmin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                when (adminAuthManager.logoutAdmin()) {
                    is AuthResponse.Success -> onSuccess()
                    is AuthResponse.Error -> onSuccess()
                    else -> {}
                }
            } catch (_: Exception) {
                onSuccess()
            }
        }
    }
}

class ProfileAdminViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileAdminViewModel::class.java)) {
            return ProfileAdminViewModel(AdminAuthManager(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
