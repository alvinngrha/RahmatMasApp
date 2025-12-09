package com.example.rahmatmas.ui.customer.purchasehistory

import android.content.Context
import android.util.Log
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

data class PurchaseHistoryUiState(
    val isLoading: Boolean = false,
    val purchases: List<SupabaseOrderWithItems> = emptyList(),
    val errorMessage: String? = null,
    val isOnline: Boolean = true,
    val stockDetails: Map<String, SupabaseStock> = emptyMap()
)

class PurchaseHistoryViewModel(
    context: Context
) : ViewModel() {

    private val supabaseClient = SupabaseModule.client
    private val orderRepository = OrderRepository()
    private val networkMonitor = NetworkMonitor(context)
    private val stockRepository = StockRepository(networkMonitor, context)

    private val _uiState = MutableStateFlow(PurchaseHistoryUiState())
    val uiState: StateFlow<PurchaseHistoryUiState> = _uiState.asStateFlow()

    init {
        // Observe connectivity to refresh when back online
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.value = _uiState.value.copy(isOnline = isOnline)
                if (isOnline) loadHistory()
            }
        }
    }

    fun loadHistory() {
        if (!_uiState.value.isOnline) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Memerlukan koneksi internet untuk memuat riwayat"
            )
            Log.e("PurchaseHistoryScreen", "Gagal membuka riwayat pembelian: tidak ada koneksi")
            return
        }

        val currentUser = supabaseClient.auth.currentUserOrNull()
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Anda harus login terlebih dahulu"
            )
            Log.e("PurchaseHistoryScreen", "Gagal membuka riwayat pembelian: belum login")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val allOrders = orderRepository.getOrdersWithItemsByUserId(currentUser.id)
                val completed = allOrders.filter { it.status == OrderStatus.COMPLETED.toDbString() }
                _uiState.value = _uiState.value.copy(isLoading = false, purchases = completed)
                loadStockDetails(completed)
            } catch (e: Exception) {
                Log.e("PurchaseHistoryScreen", "Gagal membuka riwayat pembelian: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal memuat riwayat: ${e.message}"
                )
            }
        }
    }

    private fun loadStockDetails(orders: List<SupabaseOrderWithItems>) {
        viewModelScope.launch {
            try {
                val stockIds = orders.mapNotNull { it.items.firstOrNull()?.id_stock }.distinct()
                val map = mutableMapOf<String, SupabaseStock>()
                stockIds.forEach { id ->
                    stockRepository.getStockById(id)?.let { map[id] = it }
                }
                _uiState.value = _uiState.value.copy(stockDetails = map)
            } catch (_: Exception) { /* ignore */ }
        }
    }

    fun getStockDetail(stockId: String): SupabaseStock? = _uiState.value.stockDetails[stockId]

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
