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
    val error: String? = null
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

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions.asStateFlow()

    init {
        // Monitor network status and unsynced count
        viewModelScope.launch {
            combine(
                networkMonitor.isOnline,
                offlineRepository.getAllTransactions()
            ) { isOnline, allTransactions ->
                _transactions.value = allTransactions
                val unsyncedCount = allTransactions.count { !it.isSynced }
                _uiState.value = _uiState.value.copy(
                    isOnline = isOnline,
                    unsyncedCount = unsyncedCount
                )
            }.collect { }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // In a real app, you might want to fetch data from server here
            // For now, just update the loading state
            kotlinx.coroutines.delay(500) // Simulate loading

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                snackbarMessage = "Data diperbarui"
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

    fun clearSnackbarMessage() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}