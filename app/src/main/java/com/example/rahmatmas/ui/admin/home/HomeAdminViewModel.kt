package com.example.rahmatmas.ui.admin.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.datastore.AdminAuthManager
import com.example.rahmatmas.data.supabase.AuthResponse
import kotlinx.coroutines.launch

class HomeAdminViewModel : ViewModel() {
    fun logoutAdmin(adminAuthManager: AdminAuthManager, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = adminAuthManager.logoutAdmin()
                when (result) {
                    is AuthResponse.Success -> {
                        onSuccess()
                    }

                    is AuthResponse.Error -> {
                        // Handle error jika diperlukan
                        // Untuk saat ini, kita tetap panggil onSuccess karena user harus bisa logout
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                // Handle exception, tetapi tetap logout
                onSuccess()
            }
        }
    }
}