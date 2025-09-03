package com.example.rahmatmas.ui.admin.financialreport

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.local.dao.TransactionEntity
import com.example.rahmatmas.data.local.db.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailySummary(
    val date: Date,
    val totalJual: Double,
    val totalBeli: Double
)

enum class DateRange {
    ALL, TODAY, LAST_7_DAYS, LAST_30_DAYS
}

data class FinancialReportUiState(
    val isLoading: Boolean = false,
    val summaries: List<DailySummary> = emptyList(),
    val totalJual: Double = 0.0,
    val totalBeli: Double = 0.0,
    val sortAscending: Boolean = false,
    val selectedRange: DateRange = DateRange.ALL
)

class FinancialReportAdminViewModel(
    private val context: Context
) : ViewModel() {

    private val transactionDao = AppDatabase.getDatabase(context).transactionDao()

    private val _uiState = MutableStateFlow(FinancialReportUiState(isLoading = true))
    val uiState: StateFlow<FinancialReportUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            transactionDao.getAllTransactions().collectLatest { list ->
                rebuildState(list)
            }
        }
    }

    fun toggleSortOrder() {
        val newOrder = !_uiState.value.sortAscending
        _uiState.value = _uiState.value.copy(sortAscending = newOrder)
        // Rebuild to apply sorting
        viewModelScope.launch {
            transactionDao.getAllTransactions().collectLatest { list ->
                rebuildState(list)
            }
        }
    }

    fun setRange(range: DateRange) {
        _uiState.value = _uiState.value.copy(selectedRange = range)
        viewModelScope.launch {
            transactionDao.getAllTransactions().collectLatest { list ->
                rebuildState(list)
            }
        }
    }

    private fun rebuildState(transactions: List<TransactionEntity>) {
        val filtered = filterByRange(transactions, _uiState.value.selectedRange)

        val grouped = filtered.groupBy { normalizeToDay(it.createdAt) }
        val summaries = grouped.map { (date, items) ->
            val jual = items.filter { it.jenisTransaksi.equals("Jual", ignoreCase = true) }
                .sumOf { it.totalHarga }
            val beli = items.filter { it.jenisTransaksi.equals("Beli", ignoreCase = true) }
                .sumOf { it.totalHarga }
            DailySummary(date = date, totalJual = jual, totalBeli = beli)
        }.sortedBy { it.date.time }

        val sorted = if (_uiState.value.sortAscending) summaries else summaries.reversed()

        val totalJual = filtered.filter { it.jenisTransaksi.equals("Jual", ignoreCase = true) }
            .sumOf { it.totalHarga }
        val totalBeli = filtered.filter { it.jenisTransaksi.equals("Beli", ignoreCase = true) }
            .sumOf { it.totalHarga }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            summaries = sorted,
            totalJual = totalJual,
            totalBeli = totalBeli
        )
    }

    private fun filterByRange(list: List<TransactionEntity>, range: DateRange): List<TransactionEntity> {
        val cal = Calendar.getInstance()
        val end = cal.time
        val start: Date? = when (range) {
            DateRange.ALL -> null
            DateRange.TODAY -> {
                setStartOfDay(cal)
                cal.time
            }
            DateRange.LAST_7_DAYS -> {
                setStartOfDay(cal)
                cal.add(Calendar.DAY_OF_YEAR, -6)
                cal.time
            }
            DateRange.LAST_30_DAYS -> {
                setStartOfDay(cal)
                cal.add(Calendar.DAY_OF_YEAR, -29)
                cal.time
            }
        }

        return if (start == null) list else list.filter { it.createdAt in start..end }
    }

    private fun setStartOfDay(cal: Calendar) {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
    }

    private fun normalizeToDay(date: Date): Date {
        val cal = Calendar.getInstance().apply { time = date }
        setStartOfDay(cal)
        return cal.time
    }

    fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        return sdf.format(date)
    }
}

