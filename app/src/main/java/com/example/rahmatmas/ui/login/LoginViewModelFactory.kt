package com.example.rahmatmas.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rahmatmas.data.datastore.AdminAuthManager

class LoginViewModelFactory(
    private val adminAuthManager: AdminAuthManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginAdminViewModel::class.java)) {
            return LoginAdminViewModel(adminAuthManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}