package com.example.rahmatmas.ui.customer.orderstatus

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.network.NetworkMonitor
import com.example.rahmatmas.data.repository.OrderRepository
import com.example.rahmatmas.data.repository.StockRepository
import com.example.rahmatmas.data.supabase.SupabaseModule
import com.example.rahmatmas.data.supabase.db.OrderStatus
import com.example.rahmatmas.data.supabase.db.SupabaseOrderWithItems
import com.example.rahmatmas.data.supabase.db.SupabaseStock
import com.example.rahmatmas.data.supabase.db.toDbString
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrderStatusUiState(
    val isLoading: Boolean = false,
    val orders: List<SupabaseOrderWithItems> = emptyList(),
    val userEmail: String = "",
    val userName: String = "",
    val userId: String = "",
    val errorMessage: String? = null,
    val isOnline: Boolean = true,
    val showCancelDialog: Boolean = false,
    val selectedOrderForCancel: SupabaseOrderWithItems? = null,
    val stockDetails: Map<String, SupabaseStock> = emptyMap()
)

class OrderStatusViewModel(
    context: Context
) : ViewModel() {

    private val orderRepository = OrderRepository()
    private val networkMonitor = NetworkMonitor(context)
    private val stockRepository = StockRepository(networkMonitor, context)
    private val supabaseClient = SupabaseModule.client

    private val _uiState = MutableStateFlow(OrderStatusUiState())
    val uiState: StateFlow<OrderStatusUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
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

    private fun loadUserData() {
        val currentUser = supabaseClient.auth.currentUserOrNull()
        if (currentUser != null) {
            _uiState.value = _uiState.value.copy(
                userId = currentUser.id,
                userEmail = currentUser.email ?: "",
                userName = currentUser.userMetadata?.get("full_name")?.toString()
                    ?: currentUser.userMetadata?.get("name")?.toString()
                    ?: ""
            )
        }
    }

    fun loadUserOrders() {
        if (!_uiState.value.isOnline) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Memerlukan koneksi internet untuk memuat pesanan"
            )
            return
        }

        val currentUser = supabaseClient.auth.currentUserOrNull()
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Anda harus login terlebih dahulu"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                // Use getCurrentUserOrders which should use RLS (Row Level Security)
                // or getOrdersByUserId with current user ID
                val orders = orderRepository.getOrdersWithItemsByUserId(currentUser.id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    orders = orders
                )

                // Load stock details for each order
                loadStockDetails(orders)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal memuat pesanan: ${e.message}"
                )
            }
        }
    }

    fun refreshOrders() {
        loadUserOrders()
    }

    private fun loadStockDetails(orders: List<SupabaseOrderWithItems>) {
        viewModelScope.launch {
            try {
                val stockIds = orders.mapNotNull { it.items.firstOrNull()?.id_stock }.distinct()
                val stockDetailsMap = mutableMapOf<String, SupabaseStock>()
                stockIds.forEach { stockId ->
                    stockRepository.getStockById(stockId)?.let { stock ->
                        stockDetailsMap[stockId] = stock
                    }
                }
                _uiState.value = _uiState.value.copy(stockDetails = stockDetailsMap)
            } catch (e: Exception) {
                // Ignore errors in loading stock details
            }
        }
    }

    fun getStockDetail(stockId: String): SupabaseStock? {
        return _uiState.value.stockDetails[stockId]
    }

    fun showCancelDialog(order: SupabaseOrderWithItems) {
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

    // Remove the search methods since we're now auto-loading user orders
    // Keep for backward compatibility if needed elsewhere
    fun searchOrders(phoneNumber: String) {
        // This method is no longer needed but kept for compatibility
    }

    fun clearSearch() {
        // This method is no longer needed but kept for compatibility
    }
}