package com.example.rahmatmas.ui.customer.orderstatus

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.network.NetworkMonitor
import com.example.rahmatmas.data.repository.OrderRepository
import com.example.rahmatmas.data.supabase.db.OrderStatus
import com.example.rahmatmas.data.supabase.db.SupabaseOrder
import com.example.rahmatmas.data.supabase.db.toDbString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrderStatusUiState(
    val isLoading: Boolean = false,
    val orders: List<SupabaseOrder> = emptyList(),
    val searchedPhone: String = "",
    val errorMessage: String? = null,
    val isOnline: Boolean = true,
    val showCancelDialog: Boolean = false,
    val selectedOrderForCancel: SupabaseOrder? = null
)

class OrderStatusViewModel(
    private val context: Context
) : ViewModel() {

    private val orderRepository = OrderRepository()
    private val networkMonitor = NetworkMonitor(context)

    private val _uiState = MutableStateFlow(OrderStatusUiState())
    val uiState: StateFlow<OrderStatusUiState> = _uiState.asStateFlow()

    init {
        // Monitor network status
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.value = _uiState.value.copy(isOnline = isOnline)
                if (!isOnline) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Tidak ada koneksi internet"
                    )
                }
            }
        }
    }

    fun searchOrders(phoneNumber: String) {
        if (!_uiState.value.isOnline) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Memerlukan koneksi internet untuk mencari pesanan"
            )
            return
        }

        if (phoneNumber.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Nomor HP tidak boleh kosong"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val orders = orderRepository.getOrdersByPhone(phoneNumber.trim())
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    orders = orders,
                    searchedPhone = phoneNumber.trim()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal mencari pesanan: ${e.message}"
                )
            }
        }
    }

    fun refreshOrders() {
        val currentPhone = _uiState.value.searchedPhone
        if (currentPhone.isNotBlank()) {
            searchOrders(currentPhone)
        }
    }

    fun clearSearch() {
        _uiState.value = OrderStatusUiState(
            isOnline = _uiState.value.isOnline
        )
    }

    fun showCancelDialog(order: SupabaseOrder) {
        _uiState.value = _uiState.value.copy(
            showCancelDialog = true,
            selectedOrderForCancel = order
        )
    }

    fun hideCancelDialog() {
        _uiState.value = _uiState.value.copy(
            showCancelDialog = false,
            selectedOrderForCancel = null
        )
    }

    fun cancelOrder(orderId: String, cancelReason: String) {
        viewModelScope.launch {
            if (!_uiState.value.isOnline) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Memerlukan koneksi internet untuk membatalkan pesanan"
                )
                return@launch
            }

            try {
                orderRepository.updateOrderStatus(
                    id = orderId,
                    status = OrderStatus.CANCELLED.toDbString(),
                    cancelReason = cancelReason,
                    cancelledBy = "customer"
                )

                hideCancelDialog()

                // Refresh orders to show updated status
                refreshOrders()

                _uiState.value = _uiState.value.copy(
                    errorMessage = "Pesanan berhasil dibatalkan"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Gagal membatalkan pesanan: ${e.message}"
                )
                hideCancelDialog()
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}