package com.example.rahmatmas.ui.admin.stock.stocklist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.BuildConfig
import com.example.rahmatmas.data.network.NetworkMonitor
import com.example.rahmatmas.data.repository.StockRepository
import com.example.rahmatmas.data.supabase.db.SupabaseStock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StockListUiState(
    val isLoading: Boolean = false,
    val isOnline: Boolean = true,
    val snackbarMessage: String? = null,
    val error: String? = null,
    val showPinDialog: Boolean = false,
    val pendingAction: PendingStockAction? = null
)

sealed class PendingStockAction {
    data class Edit(val stock: SupabaseStock) : PendingStockAction()
    data class Delete(val stockId: String) : PendingStockAction()
}

class StockListViewModel(
    private val context: Context
) : ViewModel() {

    private val networkMonitor = NetworkMonitor(context)
    private val stockRepository = StockRepository(
        networkMonitor = networkMonitor,
        context = context
    )

    private val _uiState = MutableStateFlow(StockListUiState())
    val uiState: StateFlow<StockListUiState> = _uiState.asStateFlow()

    private val _stocks = MutableStateFlow<List<SupabaseStock>>(emptyList())
    val stocks: StateFlow<List<SupabaseStock>> = _stocks.asStateFlow()

    private var currentQuery: String = ""

    init {
        // Monitor network status
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.value = _uiState.value.copy(isOnline = isOnline)
                if (isOnline) {
                    loadStocks()
                } else {
                    _stocks.value = emptyList()
                }
            }
        }
    }

    private fun loadStocks() {
        if (currentQuery.isEmpty()) {
            getAllStocks()
        } else {
            searchStocks(currentQuery)
        }
    }

    private fun getAllStocks() {
        viewModelScope.launch {
            if (!_uiState.value.isOnline) {
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "Tidak ada koneksi internet"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                stockRepository.getAllStocks().collect { stockList ->
                    _stocks.value = stockList
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Gagal memuat data stok: ${e.message}",
                    snackbarMessage = "Gagal memuat data stok"
                )
            }
        }
    }

    fun searchStocks(query: String) {
        currentQuery = query

        if (!_uiState.value.isOnline) {
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Tidak ada koneksi internet"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                if (query.isEmpty()) {
                    stockRepository.getAllStocks().collect { stockList ->
                        _stocks.value = stockList
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                } else {
                    stockRepository.searchStocks(query).collect { stockList ->
                        _stocks.value = stockList
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Gagal mencari stok: ${e.message}",
                    snackbarMessage = "Gagal mencari stok"
                )
            }
        }
    }

    fun refreshData() {
        loadStocks()
    }

    // PIN Access Methods
    fun requestEditStock(stock: SupabaseStock) {
        _uiState.value = _uiState.value.copy(
            showPinDialog = true,
            pendingAction = PendingStockAction.Edit(stock)
        )
    }

    fun requestDeleteStock(stockId: String) {
        _uiState.value = _uiState.value.copy(
            showPinDialog = true,
            pendingAction = PendingStockAction.Delete(stockId)
        )
    }

    fun validatePin(pin: String): Boolean {
        return pin == BuildConfig.STOCK_ACCESS_PIN
    }

    fun executePendingAction() {
        val action = _uiState.value.pendingAction ?: return

        when (action) {
            is PendingStockAction.Edit -> {
                // Action will be handled by navigation in the screen
                // Just keep the action for the screen to use
            }
            is PendingStockAction.Delete -> {
                deleteStock(action.stockId)
            }
        }

        // Clear pending action after execution
        _uiState.value = _uiState.value.copy(pendingAction = null)
    }

    fun dismissPinDialog() {
        _uiState.value = _uiState.value.copy(
            showPinDialog = false,
            pendingAction = null
        )
    }

    fun updateStock(stock: SupabaseStock) {
        viewModelScope.launch {
            if (!_uiState.value.isOnline) {
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "Tidak ada koneksi internet"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = stockRepository.updateStock(stock)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        snackbarMessage = "Stok berhasil diperbarui"
                    )
                    loadStocks()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Gagal memperbarui stok: ${exception.message}",
                        snackbarMessage = "Gagal memperbarui stok"
                    )
                }
            )
        }
    }

    fun deleteStock(stockId: String) {
        viewModelScope.launch {
            if (!_uiState.value.isOnline) {
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "Tidak ada koneksi internet"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = stockRepository.deleteStock(stockId)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        snackbarMessage = "Stok berhasil dihapus"
                    )
                    // Refresh data after deletion
                    loadStocks()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Gagal menghapus stok: ${exception.message}",
                        snackbarMessage = "Gagal menghapus stok"
                    )
                }
            )
        }
    }

    fun clearSnackbarMessage() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
