package com.example.rahmatmas.ui.admin.onlinesale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.repository.OrderRepository
import com.example.rahmatmas.data.supabase.db.SupabaseOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnlineSaleUiState(
    val isLoading: Boolean = false,
    val orders: List<SupabaseOrder> = emptyList(),
    val errorMessage: String? = null
)

class OnlineSaleAdminViewModel : ViewModel() {
    private val orderRepository = OrderRepository()

    private val _uiState = MutableStateFlow(OnlineSaleUiState())
    val uiState: StateFlow<OnlineSaleUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val orders = orderRepository.getOrders()
                _uiState.value = OnlineSaleUiState(orders = orders)
            } catch (e: Exception) {
                _uiState.value = OnlineSaleUiState(errorMessage = e.message)
            }
        }
    }

    fun updateStatus(id: String, status: String) {
        viewModelScope.launch {
            try {
                orderRepository.updateOrderStatus(id, status)
                loadOrders()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
}