package com.example.rahmatmas.ui.costumer.catalog

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

data class CatalogUiState(
    val isLoading: Boolean = false,
    val isOnline: Boolean = true,
    val errorMessage: String? = null,
    val searchQuery: String = ""
)

class CatalogViewModel(
    private val context: Context
) : ViewModel() {

    private val networkMonitor = NetworkMonitor(context)
    private val stockRepository = StockRepository(
        networkMonitor = networkMonitor,
        context = context
    )

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private val _stocks = MutableStateFlow<List<SupabaseStock>>(emptyList())
    val stocks: StateFlow<List<SupabaseStock>> = _stocks.asStateFlow()

    private var allStocks: List<SupabaseStock> = emptyList()

    init {
        // Monitor network status
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.value = _uiState.value.copy(isOnline = isOnline)
                if (isOnline) {
                    loadAvailableStocks()
                } else {
                    _stocks.value = emptyList()
                }
            }
        }
    }

    private fun loadAvailableStocks() {
        viewModelScope.launch {
            if (!_uiState.value.isOnline) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Tidak ada koneksi internet"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                stockRepository.getAllStocks().collect { stockList ->
                    // Filter only available stocks (stock > 0)
                    val availableStocks = stockList.filter { it.jumlah_stok > 0 }
                    allStocks = availableStocks

                    // Apply search filter if any
                    applySearchFilter()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal memuat katalog: ${e.message}"
                )
            }
        }
    }

    fun searchStocks(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applySearchFilter()
    }

    private fun applySearchFilter() {
        val query = _uiState.value.searchQuery
        val filteredStocks = if (query.isEmpty()) {
            allStocks
        } else {
            allStocks.filter { stock ->
                stock.nama_barang.contains(query, ignoreCase = true) ||
                        stock.kadar_emas.contains(query, ignoreCase = true) ||
                        stock.kadar_persen.contains(query, ignoreCase = true)
            }
        }
        _stocks.value = filteredStocks
    }

    fun refreshCatalog() {
        loadAvailableStocks()
    }

    // Get stock by ID for order flow
    suspend fun getStockById(stockId: String): SupabaseStock? {
        return try {
            stockRepository.getStockById(stockId)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Gagal mengambil data barang: ${e.message}"
            )
            null
        }
    }

    // Get stocks by category (kadar emas)
    fun filterByKadarEmas(kadarEmas: String) {
        val filteredStocks = if (kadarEmas.isEmpty()) {
            allStocks
        } else {
            allStocks.filter { it.kadar_emas == kadarEmas }
        }
        _stocks.value = filteredStocks
    }

    // Get available kadar emas options for filter
    fun getAvailableKadarEmas(): List<String> {
        return allStocks.map { it.kadar_emas }.distinct().sorted()
    }

    // Sort stocks
    fun sortStocks(sortBy: SortOption) {
        val currentStocks = _stocks.value.toMutableList()

        when (sortBy) {
            SortOption.NAME_ASC -> currentStocks.sortBy { it.nama_barang }
            SortOption.NAME_DESC -> currentStocks.sortByDescending { it.nama_barang }
            SortOption.KADAR_ASC -> currentStocks.sortBy { it.kadar_emas.toIntOrNull() ?: 0 }
            SortOption.KADAR_DESC -> currentStocks.sortByDescending { it.kadar_emas.toIntOrNull() ?: 0 }
            SortOption.WEIGHT_ASC -> currentStocks.sortBy { it.berat_emas }
            SortOption.WEIGHT_DESC -> currentStocks.sortByDescending { it.berat_emas }
            SortOption.STOCK_ASC -> currentStocks.sortBy { it.jumlah_stok }
            SortOption.STOCK_DESC -> currentStocks.sortByDescending { it.jumlah_stok }
            SortOption.NEWEST -> currentStocks.sortByDescending { it.created_at }
            SortOption.OLDEST -> currentStocks.sortBy { it.created_at }
        }

        _stocks.value = currentStocks
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

enum class SortOption {
    NAME_ASC,
    NAME_DESC,
    KADAR_ASC,
    KADAR_DESC,
    WEIGHT_ASC,
    WEIGHT_DESC,
    STOCK_ASC,
    STOCK_DESC,
    NEWEST,
    OLDEST
}