package com.example.rahmatmas.ui.customer.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.repository.OrderRepository
import com.example.rahmatmas.data.supabase.db.SupabaseOrder
import com.example.rahmatmas.data.supabase.db.SupabaseStock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

data class CheckoutUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successOrderId: String? = null
)

class CheckoutViewModel : ViewModel() {
    private val orderRepository = OrderRepository()

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun placeOrder(
        stock: SupabaseStock,
        name: String,
        address: String,
        phone: String,
        note: String?,
        shippingOption: String
    ) {
        viewModelScope.launch {
            _uiState.value = CheckoutUiState(isLoading = true)

            // Validate inputs
            if (name.isBlank() || address.isBlank() || phone.isBlank()) {
                _uiState.value = CheckoutUiState(
                    errorMessage = "Semua field wajib diisi (kecuali catatan)"
                )
                return@launch
            }

            if (phone.length < 10) {
                _uiState.value = CheckoutUiState(
                    errorMessage = "Nomor HP tidak valid"
                )
                return@launch
            }

            try {
                val orderId = "ORD-${UUID.randomUUID()}"
                val currentTime = Instant.now().toString()

                val order = SupabaseOrder(
                    id = orderId,
                    stock_id = stock.id_barang,
                    stock_name = stock.nama_barang,
                    recipient_name = name.trim(),
                    address = address.trim(),
                    phone = phone.trim(),
                    note = note?.trim()?.takeIf { it.isNotBlank() },
                    shipping_option = shippingOption,
                    status = "menunggu konfirmasi",
                    created_at = currentTime,
                    updated_at = currentTime,
                    cancel_reason = null,
                    cancelled_by = null
                )

                orderRepository.placeOrder(order)

                _uiState.value = CheckoutUiState(
                    isSuccess = true,
                    successOrderId = orderId
                )
            } catch (e: Exception) {
                _uiState.value = CheckoutUiState(
                    errorMessage = "Gagal memproses pesanan: ${e.message}"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = CheckoutUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}