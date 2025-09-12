package com.example.rahmatmas.ui.admin.transactionhistory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.local.dao.TransactionEntity
import com.example.rahmatmas.data.local.db.AppDatabase
import com.example.rahmatmas.data.network.NetworkMonitor
import com.example.rahmatmas.data.repository.OfflineTransactionRepository
import com.example.rahmatmas.util.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Date

data class TransactionHistoryUiState(
    val isLoading: Boolean = false,
    val isOnline: Boolean = true,
    val unsyncedCount: Int = 0,
    val snackbarMessage: String? = null,
    val error: String? = null,
    // Date range filter
    val startDate: Date? = null,
    val endDate: Date? = null,
    // Multi-select
    val selectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
    // Search only
    val searchQuery: String = ""
)

class TransactionHistoryViewModel(
    private val context: Context
) : ViewModel() {

    private val database = AppDatabase.getDatabase(context)
    private val networkMonitor = NetworkMonitor(context)
    private val offlineRepository = OfflineTransactionRepository(
        transactionDao = database.transactionDao(),
        networkMonitor = networkMonitor,
        context = context
    )
    private val pdfGenerator = PdfGenerator(context)

    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState: StateFlow<TransactionHistoryUiState> = _uiState.asStateFlow()

    private val _allTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions.asStateFlow()

    init {
        // Monitor network status and unsynced count
        viewModelScope.launch {
            combine(
                networkMonitor.isOnline,
                offlineRepository.getAllTransactions()
            ) { isOnline, allTransactions ->
                _allTransactions.value = allTransactions
                val unsyncedCount = allTransactions.count { !it.isSynced }
                _uiState.value = _uiState.value.copy(
                    isOnline = isOnline,
                    unsyncedCount = unsyncedCount
                )
                applyFilters()
            }.collect { }
        }
    }

    private fun applyFilters() {
        val current = _allTransactions.value
        val start = _uiState.value.startDate
        val end = _uiState.value.endDate
        val query = _uiState.value.searchQuery.trim().lowercase()

        val filtered = current.filter { tx ->
            val created = tx.createdAt.time
            val afterStart = start?.let { created >= atStartOfDayMillis(it) } ?: true
            val beforeEnd = end?.let { created <= atEndOfDayMillis(it) } ?: true
            val matchesQuery = if (query.isEmpty()) true else {
                tx.namaBarang.lowercase().contains(query) ||
                tx.id.lowercase().contains(query) ||
                tx.jenisTransaksi.lowercase().contains(query)
            }
            afterStart && beforeEnd && matchesQuery
        }
        // Default ordering: latest first
        _transactions.value = filtered.sortedByDescending { it.createdAt }
        // Clean up selected ids that are no longer visible
        if (_uiState.value.selectedIds.isNotEmpty()) {
            val visibleIds = filtered.map { it.id }.toSet()
            _uiState.value = _uiState.value.copy(
                selectedIds = _uiState.value.selectedIds.intersect(visibleIds)
            )
        }
    }

    private fun atStartOfDayMillis(date: Date): Long {
        val cal = java.util.Calendar.getInstance().apply {
            time = date
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun atEndOfDayMillis(date: Date): Long {
        val cal = java.util.Calendar.getInstance().apply {
            time = date
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    fun refreshData() {
        viewModelScope.launch {
            if (!_uiState.value.isOnline) {
                // Tetap tampilkan data lokal + filter tanpa memaksa sync
                applyFilters()
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "Tidak ada koneksi internet"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = offlineRepository.forcSync()

            result.fold(
                onSuccess = {
                    // Pastikan daftar yang terlihat mengikuti filter terbaru
                    applyFilters()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        snackbarMessage = "Data diperbarui"
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Gagal memperbarui: ${exception.message}",
                        snackbarMessage = "Gagal memperbarui"
                    )
                }
            )
        }
    }

    fun syncTransactions() {
        viewModelScope.launch {
            if (!_uiState.value.isOnline) {
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "Tidak ada koneksi internet"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = offlineRepository.forcSync()

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        snackbarMessage = "Sinkronisasi berhasil"
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Gagal sinkronisasi: ${exception.message}",
                        snackbarMessage = "Gagal sinkronisasi"
                    )
                }
            )
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = offlineRepository.updateTransaction(transaction)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        snackbarMessage = "Transaksi berhasil diperbarui"
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Gagal memperbarui transaksi: ${exception.message}",
                        snackbarMessage = "Gagal memperbarui transaksi"
                    )
                }
            )
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = offlineRepository.deleteTransaction(transactionId)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        snackbarMessage = "Transaksi berhasil dihapus"
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Gagal menghapus transaksi: ${exception.message}",
                        snackbarMessage = "Gagal menghapus transaksi"
                    )
                }
            )
        }
    }

    fun exportSingleToPdf(transaction: TransactionEntity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val result = pdfGenerator.generateSingleTransactionReceipt(transaction)

                result.fold(
                    onSuccess = { filePath ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
//                            snackbarMessage = "PDF berhasil dibuat: ${filePath.substringAfterLast("/")}"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Gagal membuat PDF: ${exception.message}",
                            snackbarMessage = "Gagal membuat PDF"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan: ${e.message}",
                    snackbarMessage = "Terjadi kesalahan saat membuat PDF"
                )
            }
        }
    }

    fun exportAllToPdf() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Get current transactions
                val currentTransactions = _transactions.value

                if (currentTransactions.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        snackbarMessage = "Tidak ada transaksi untuk diekspor"
                    )
                    return@launch
                }

                val result = pdfGenerator.generateMultipleTransactionReport(currentTransactions)

                result.fold(
                    onSuccess = { filePath ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            snackbarMessage = "Laporan PDF berhasil dibuat: ${filePath.substringAfterLast("/")}"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Gagal membuat laporan PDF: ${exception.message}",
                            snackbarMessage = "Gagal membuat laporan PDF"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan: ${e.message}",
                    snackbarMessage = "Terjadi kesalahan saat membuat laporan"
                )
            }
        }
    }

    fun exportByDateRange(startDate: Date, endDate: Date) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val transactionsInRange = offlineRepository.getTransactionsByDateRange(startDate, endDate)

                if (transactionsInRange.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        snackbarMessage = "Tidak ada transaksi dalam rentang tanggal yang dipilih"
                    )
                    return@launch
                }

                val result = pdfGenerator.generateSingleTransactionReceipt(transaction = transactionsInRange.first())

                result.fold(
                    onSuccess = { filePath ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            snackbarMessage = "Laporan berhasil dibuat: ${filePath.substringAfterLast("/")}"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Gagal membuat laporan: ${exception.message}",
                            snackbarMessage = "Gagal membuat laporan"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan: ${e.message}",
                    snackbarMessage = "Terjadi kesalahan saat membuat laporan"
                )
            }
        }
    }

    // Date range filter controls
    fun setStartDate(date: Date?) {
        _uiState.value = _uiState.value.copy(startDate = date)
        applyFilters()
    }

    fun setEndDate(date: Date?) {
        _uiState.value = _uiState.value.copy(endDate = date)
        applyFilters()
    }

    fun clearDateFilter() {
        _uiState.value = _uiState.value.copy(startDate = null, endDate = null)
        applyFilters()
    }

    // Selection controls
    fun toggleSelectionMode() {
        val newMode = !_uiState.value.selectionMode
        _uiState.value = _uiState.value.copy(
            selectionMode = newMode,
            selectedIds = if (newMode) _uiState.value.selectedIds else emptySet()
        )
    }

    fun toggleSelect(transactionId: String) {
        val current = _uiState.value.selectedIds.toMutableSet()
        if (current.contains(transactionId)) current.remove(transactionId) else current.add(transactionId)
        _uiState.value = _uiState.value.copy(selectedIds = current)
    }

    fun selectAllVisible() {
        val allVisible = _transactions.value.map { it.id }.toSet()
        _uiState.value = _uiState.value.copy(selectedIds = allVisible)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedIds = emptySet())
    }

    fun exportSelectedToPdf() {
        viewModelScope.launch {
            val selected = _uiState.value.selectedIds
            if (selected.isEmpty()) {
                _uiState.value = _uiState.value.copy(snackbarMessage = "Pilih transaksi terlebih dahulu")
                return@launch
            }
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val list = _transactions.value.filter { it.id in selected }
                val result = pdfGenerator.generateMultipleTransactionReport(list)
                result.fold(
                    onSuccess = { filePath ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            snackbarMessage = "Laporan PDF berhasil dibuat: ${filePath.substringAfterLast("/")}"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Gagal membuat laporan PDF: ${exception.message}",
                            snackbarMessage = "Gagal membuat laporan PDF"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan: ${e.message}",
                    snackbarMessage = "Terjadi kesalahan saat membuat laporan"
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }


    fun clearSnackbarMessage() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
