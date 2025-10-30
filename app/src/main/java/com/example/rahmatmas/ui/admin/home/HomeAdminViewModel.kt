package com.example.rahmatmas.ui.admin.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.datastore.AdminAuthManager
import com.example.rahmatmas.data.local.dao.TransactionDao
import com.example.rahmatmas.data.local.dao.TransactionEntity
import com.example.rahmatmas.data.repository.GoldPriceRepository
import com.example.rahmatmas.data.repository.OrderRepository
import com.example.rahmatmas.data.repository.StockRepository
import com.example.rahmatmas.data.supabase.AuthResponse
import com.example.rahmatmas.data.supabase.db.SupabaseOrderWithItems
import com.example.rahmatmas.data.supabase.db.SupabaseStock
import com.example.rahmatmas.data.supabase.db.toOrderStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale


data class GoldPriceUiState(
    val buyPrice: String = "Loading...",
    val sellPrice: String = "Loading...",
    val isLoading: Boolean = true,
    val error: String? = null
)

enum class ActivityType { TRANSACTION, STOCK, ONLINE_ORDER }

data class RecentActivityItem(
    val type: ActivityType,
    val title: String,
    val subtitle: String,
    val highlight: String? = null,
    val timestamp: String? = null
)

data class RecentActivityUiState(
    val isLoading: Boolean = true,
    val activities: List<RecentActivityItem> = emptyList(),
    val error: String? = null
)

class HomeAdminViewModel(
    private val adminAuthManager: AdminAuthManager,
    private val goldPriceRepository: GoldPriceRepository,
    private val transactionDao: TransactionDao,
    private val stockRepository: StockRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _goldPriceState = MutableStateFlow(GoldPriceUiState())
    val goldPriceState: StateFlow<GoldPriceUiState> = _goldPriceState.asStateFlow()

    private val _recentActivityState = MutableStateFlow(RecentActivityUiState())
    val recentActivityState: StateFlow<RecentActivityUiState> = _recentActivityState.asStateFlow()

    private val timestampFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy • HH.mm", Locale("id", "ID"))

    init {
        fetchGoldPrices()
        refreshActivities()
    }

    fun logoutAdmin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = adminAuthManager.logoutAdmin()
                when (result) {
                    is AuthResponse.Success -> {
                        onSuccess()
                    }
                    is AuthResponse.Error -> {
                        onSuccess()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onSuccess()
            }
        }
    }

    fun fetchGoldPrices() {
        viewModelScope.launch {
            _goldPriceState.value = _goldPriceState.value.copy(isLoading = true, error = null)

            try {
                val response = goldPriceRepository.getGoldPrice()
                if (response.isSuccessful) {
                    val goldPriceResponse = response.body()
                    val goldData = goldPriceRepository.extractGoldMetalPrice(goldPriceResponse)

                    if (goldData != null) {
                        val buyPrice = goldData.bid?.let { formatCurrency(it) }
                            ?: goldData.price?.let { formatCurrency(it) }
                            ?: "Data tidak tersedia"

                        val sellPrice = goldData.ask?.let { formatCurrency(it) }
                            ?: goldData.price?.let { formatCurrency(it) }
                            ?: "Data tidak tersedia"

                        _goldPriceState.value = GoldPriceUiState(
                            buyPrice = buyPrice,
                            sellPrice = sellPrice,
                            isLoading = false,
                            error = null,
                        )
                    } else {
                        _goldPriceState.value = GoldPriceUiState(
                            buyPrice = "Data tidak tersedia",
                            sellPrice = "Data tidak tersedia",
                            isLoading = false,
                            error = "Data emas tidak ditemukan"
                        )
                    }
                } else {
                    _goldPriceState.value = GoldPriceUiState(
                        buyPrice = "Gagal memuat",
                        sellPrice = "Gagal memuat",
                        isLoading = false,
                        error = "Gagal mengambil data: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _goldPriceState.value = GoldPriceUiState(
                    buyPrice = "Tidak ada internet",
                    sellPrice = "Tidak ada internet",
                    isLoading = false,
                    error = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    fun refreshData() {
        fetchGoldPrices()
    }

    fun refreshActivities() {
        viewModelScope.launch {
            _recentActivityState.value = recentActivityState.value.copy(isLoading = true, error = null)

            val result = runCatching {
                val latestTransaction = withContext(Dispatchers.IO) {
                    transactionDao.getAllTransactions().firstOrNull()?.firstOrNull()
                }

                val latestStock = withContext(Dispatchers.IO) {
                    stockRepository.getAllStocks().firstOrNull()
                        ?.sortedWith(compareByDescending<SupabaseStock> { it.updated_at ?: it.created_at ?: "" })
                        ?.firstOrNull()
                }

                val latestOrder = withContext(Dispatchers.IO) {
                    orderRepository.getOrdersWithItems().firstOrNull()
                }

                buildRecentActivities(latestTransaction, latestStock, latestOrder)
            }

            result.onSuccess { activities ->
                _recentActivityState.value = RecentActivityUiState(
                    isLoading = false,
                    activities = activities,
                    error = null
                )
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable

                _recentActivityState.value = RecentActivityUiState(
                    isLoading = false,
                    activities = buildRecentActivities(null, null, null),
                    error = throwable.message ?: "Gagal memuat aktivitas terbaru"
                )
            }
        }
    }

    private fun buildRecentActivities(
        transaction: TransactionEntity?,
        stock: SupabaseStock?,
        order: SupabaseOrderWithItems?
    ): List<RecentActivityItem> {
        val activities = mutableListOf<RecentActivityItem>()

        activities += if (transaction != null) {
            RecentActivityItem(
                type = ActivityType.TRANSACTION,
                title = "Transaksi Terbaru",
                subtitle = "${transaction.namaBarang} • ${transaction.jenisTransaksi}",
                highlight = formatCurrency(transaction.totalHarga),
                timestamp = formatDate(transaction.createdAt)
            )
        } else {
            RecentActivityItem(
                type = ActivityType.TRANSACTION,
                title = "Transaksi Terbaru",
                subtitle = "Belum ada transaksi terbaru",
                highlight = null,
                timestamp = null
            )
        }

        activities += if (stock != null) {
            val highlight = listOfNotNull(stock.kadar_emas, stock.kadar_persen.takeIf { it.isNotBlank() })
                .joinToString(separator = " • ")

            RecentActivityItem(
                type = ActivityType.STOCK,
                title = "Stok Diperbarui",
                subtitle = "${stock.nama_barang} • ${stock.jumlah_stok} pcs",
                highlight = highlight.ifBlank { null },
                timestamp = formatIsoString(stock.updated_at ?: stock.created_at)
            )
        } else {
            RecentActivityItem(
                type = ActivityType.STOCK,
                title = "Stok Diperbarui",
                subtitle = "Belum ada update stok terbaru",
                highlight = null,
                timestamp = null
            )
        }

        activities += if (order != null) {
            val primaryItem = order.items.firstOrNull()
            val totalQuantity = order.items.sumOf { it.jumlah_order }
            val productLabel = primaryItem?.nama_stock ?: "Pesanan"
            val quantityLabel = if (totalQuantity > 0) " ($totalQuantity pcs)" else ""

            RecentActivityItem(
                type = ActivityType.ONLINE_ORDER,
                title = "Pesanan Online",
                subtitle = "${order.recipient_name} • $productLabel$quantityLabel",
                highlight = order.status.toOrderStatus().displayName,
                timestamp = formatIsoString(order.created_at ?: order.updated_at)
            )
        } else {
            RecentActivityItem(
                type = ActivityType.ONLINE_ORDER,
                title = "Pesanan Online",
                subtitle = "Belum ada pesanan online masuk",
                highlight = null,
                timestamp = null
            )
        }

        return activities
    }

    private fun formatDate(date: Date?): String? {
        return date?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.let { timestampFormatter.format(it) }
    }

    private fun formatIsoString(isoString: String?): String? {
        if (isoString.isNullOrBlank()) return null
        val temporal = parseIsoDate(isoString) ?: return null
        return timestampFormatter.format(temporal)
    }

    private fun parseIsoDate(value: String): OffsetDateTime? {
        return try {
            OffsetDateTime.parse(value)
        } catch (e: DateTimeParseException) {
            try {
                Instant.parse(value).atOffset(ZoneOffset.UTC)
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount).replace("Rp", "Rp ")
    }
}
