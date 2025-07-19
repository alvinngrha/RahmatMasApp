package com.example.rahmatmas.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.supabase.AuthManager
import com.example.rahmatmas.data.supabase.AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)

class LoginCostumerViewModel : ViewModel() {
    private val authManager = AuthManager()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        _uiState.value = _uiState.value.copy(isLoggedIn = authManager.isLoggedIn())
    }

    fun signInWithGoogle(context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = authManager.signInGoogle(context)) {
                is AuthResponse.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        errorMessage = null
                    )
                    onSuccess()
                }
                is AuthResponse.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}