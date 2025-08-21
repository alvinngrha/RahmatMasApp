package com.example.rahmatmas.ui.admin.onlinesale

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.network.NetworkMonitor
import com.example.rahmatmas.data.repository.OrderRepository
import com.example.rahmatmas.data.repository.StockRepository
import com.example.rahmatmas.data.supabase.db.OrderStatus
import com.example.rahmatmas.data.supabase.db.SupabaseOrder
import com.example.rahmatmas.data.supabase.db.SupabaseStock
import com.example.rahmatmas.data.supabase.db.toDbString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnlineSaleUiState(
    val isLoading: Boolean = false,
    val allOrders: List<SupabaseOrder> = emptyList(),
    val selectedTabIndex: Int = 0,
    val errorMessage: String? = null,
    val isOnline: Boolean = true,
    val stockDetails: Map<String, SupabaseStock> = emptyMap(),
    val showCancelDialog: Boolean = false,
    val selectedOrderForCancel: SupabaseOrder? = null
)

class OnlineSaleAdminViewModel(
    context: Context
) : ViewModel() {

    private val orderRepository = OrderRepository()
    private val networkMonitor = NetworkMonitor(context)
    private val stockRepository = StockRepository(networkMonitor, context)

    private val _uiState = MutableStateFlow(OnlineSaleUiState())
    val uiState: StateFlow<OnlineSaleUiState> = _uiState.asStateFlow()

    val tabTitles = listOf(
        "Menunggu (${getOrdersByStatus(OrderStatus.PENDING).size})",
        "Diproses (${getOrdersByStatus(OrderStatus.PROCESSING).size})",
        "Dikirim (${getOrdersByStatus(OrderStatus.SHIPPING).size})",
        "Selesai (${getOrdersByStatus(OrderStatus.COMPLETED).size})",
        "Dibatalkan (${getOrdersByStatus(OrderStatus.CANCELLED).size})"
    )

    init {
        // Monitor network status
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.value = _uiState.value.copy(isOnline = isOnline)
                if (isOnline) {
                    loadOrders()
                }
            }
        }
    }

    fun loadOrders() {
        if (!_uiState.value.isOnline) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val orders = orderRepository.getOrders()
                _uiState.value = _uiState.value.copy(
                    allOrders = orders,
                    isLoading = false,
                    errorMessage = null
                )

                // Load stock details for each order
                loadStockDetails(orders)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    private fun loadStockDetails(orders: List<SupabaseOrder>) {
        viewModelScope.launch {
            try {
                val stockIds = orders.map { it.stock_id }.distinct()
                val stockDetailsMap = mutableMapOf<String, SupabaseStock>()

                stockIds.forEach { stockId ->
                    stockRepository.getStockById(stockId)?.let { stock ->
                        stockDetailsMap[stockId] = stock
                    }
                }

                _uiState.value = _uiState.value.copy(stockDetails = stockDetailsMap)
            } catch (e: Exception) {
                // Handle error silently for stock details
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTabIndex = index)
    }

    fun getOrdersByStatus(status: OrderStatus): List<SupabaseOrder> {
        return _uiState.value.allOrders.filter {
            it.status.lowercase() == status.toDbString()
        }
    }

    fun getCurrentTabOrders(): List<SupabaseOrder> {
        return when (_uiState.value.selectedTabIndex) {
            0 -> getOrdersByStatus(OrderStatus.PENDING)
            1 -> getOrdersByStatus(OrderStatus.PROCESSING)
            2 -> getOrdersByStatus(OrderStatus.SHIPPING)
            3 -> getOrdersByStatus(OrderStatus.COMPLETED)
            4 -> getOrdersByStatus(OrderStatus.CANCELLED)
            else -> emptyList()
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            try {
                orderRepository.updateOrderStatus(orderId, newStatus.toDbString())
                loadOrders() // Refresh orders
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
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
            try {
                orderRepository.updateOrderStatus(
                    id = orderId,
                    status = OrderStatus.CANCELLED.toDbString(),
                    cancelReason = cancelReason,
                    cancelledBy = "admin"
                )
                hideCancelDialog()
                loadOrders() // Refresh orders
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun getStockDetail(stockId: String): SupabaseStock? {
        return _uiState.value.stockDetails[stockId]
    }
}