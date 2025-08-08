package com.example.rahmatmas.ui.admin.stock.stocklist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val error: String? = null
)

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