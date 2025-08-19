package com.example.rahmatmas.ui.costumer.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.repository.OrderRepository
import com.example.rahmatmas.data.supabase.db.SupabaseOrder
import com.example.rahmatmas.data.supabase.db.SupabaseStock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class CheckoutUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
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
            try {
                val id = "ORD-${UUID.randomUUID()}"
                val order = SupabaseOrder(
                    id = id,
                    stock_id = stock.id_barang,
                    stock_name = stock.nama_barang,
                    recipient_name = name,
                    address = address,
                    phone = phone,
                    note = note,
                    shipping_option = shippingOption,
                    status = "menunggu konfirmasi"
                )
                orderRepository.placeOrder(order)
                _uiState.value = CheckoutUiState(isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = CheckoutUiState(errorMessage = e.message)
            }
        }
    }

    fun resetState() {
        _uiState.value = CheckoutUiState()
    }
}