package com.example.rahmatmas.ui.admin.transactionrecording


import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.local.dao.TransactionEntity
import com.example.rahmatmas.data.local.db.AppDatabase
import com.example.rahmatmas.data.network.NetworkMonitor
import com.example.rahmatmas.data.repository.GoldPriceRepository
import com.example.rahmatmas.data.repository.PhotoUploadRepository
import com.example.rahmatmas.data.repository.OfflineTransactionRepository
import com.example.rahmatmas.data.repository.StockRepository
import com.example.rahmatmas.data.supabase.db.SupabaseStock
import com.example.rahmatmas.util.PdfGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

data class TransactionUiState(
    val idTransaksi: String = "",
    val namaBarang: String = "",
    val jumlahBarang: String = "",
    val kadarEmas: String = "",
    val jenisTransaksi: String = "",
    val beratEmas: String = "",
    val ongkos: String = "",
    val hargaDasarPerGram: String = "",
    val totalHarga: Double = 0.0,
    // Search & autofill from stock
    val searchQuery: String = "",
    val showSearchResults: Boolean = false,
    val searchResults: List<SupabaseStock> = emptyList(),
    val selectedStock: SupabaseStock? = null,
    val goldPrice: Double? = null,
    val isKadarEmasExpanded: Boolean = false,
    val isJenisTransaksiExpanded: Boolean = false,
    val snackbarMessage: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val beratError: String? = null,
    val hargaDasarError: String? = null,
    val ongkosError: String? = null,
    val jumlahBarangError: String? = null,
    val photoUri: Uri? = null,
    val isOnline: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val lastSaveWasOnline: Boolean? = null,
    val unsyncedCount: Int = 0
)

class TransactionRecordingViewModel(
    private val context: Context
) : ViewModel() {

    private val database = AppDatabase.getDatabase(context)
    private val networkMonitor = NetworkMonitor(context)
    private val offlineRepository = OfflineTransactionRepository(
        transactionDao = database.transactionDao(),
        networkMonitor = networkMonitor,
        context = context
    )
    private val stockRepository = StockRepository(networkMonitor, context)
    private val goldPriceRepository = GoldPriceRepository()
    private val pdfGenerator = PdfGenerator(context)
    private val photoUploadRepository = PhotoUploadRepository(context)

    private val _transactionUiState = MutableStateFlow(TransactionUiState())
    val transactionUiState: StateFlow<TransactionUiState> = _transactionUiState.asStateFlow()

    // Current network status
    private var isCurrentlyOnline = true
    private var searchJob: Job? = null

    init {
        // Monitor network status
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                isCurrentlyOnline = isOnline
                _transactionUiState.value = _transactionUiState.value.copy(isOnline = isOnline)

                // Update unsynced count
                if (isOnline) {
                    updateUnsyncedCount()
                    // Prefetch gold price for faster autofill
                    fetchGoldPrice()
                }
            }
        }

        // Initial unsynced count
        updateUnsyncedCount()
    }

    private fun updateUnsyncedCount() {
        viewModelScope.launch {
            val count = offlineRepository.getUnsyncedCount()
            _transactionUiState.value = _transactionUiState.value.copy(unsyncedCount = count)
        }
    }

    // Update nama barang
    fun updateNamaBarang(nama: String) {
        if (nama.isNotEmpty()) {
            _transactionUiState.value = _transactionUiState.value.copy(namaBarang = nama, error = null)
        } else {
            // Tampilkan pesan error jika nama barang kosong
            _transactionUiState.value = _transactionUiState.value.copy(
                namaBarang = nama,
                error = "Nama barang tidak boleh kosong"
            )
        }
    }

    fun updateJumlahBarang(jumlah: String) {
        if (jumlah.all { it.isDigit() } || jumlah.isEmpty()) {
            _transactionUiState.value =
                _transactionUiState.value.copy(jumlahBarang = jumlah, jumlahBarangError = null)

            // Hitung ulang total harga jika berat emas sudah diisi
            if (_transactionUiState.value.jumlahBarang.isNotEmpty()) {
                calculateTotalHarga()
            } else {
                _transactionUiState.value = _transactionUiState.value.copy(totalHarga = 0.0)
            }
        } else {
            // Tampilkan pesan error jika jumlah barang tidak valid
            _transactionUiState.value = _transactionUiState.value.copy(
                jumlahBarang = jumlah,
                jumlahBarangError = "Jumlah barang harus berupa angka"
            )
        }
    }

    // Update kadar emas
    fun updateKadarEmas(kadar: String) {
        _transactionUiState.value = _transactionUiState.value.copy(kadarEmas = kadar)
    }

    fun updateKadarEmasExpanded(isExpanded: Boolean) {
        _transactionUiState.value = _transactionUiState.value.copy(isKadarEmasExpanded = isExpanded)
    }

    // Update jenis transaksi
    fun updateJenisTransaksi(jenis: String) {
        _transactionUiState.value = _transactionUiState.value.copy(jenisTransaksi = jenis)
    }

    fun updateJenisTransaksiExpanded(isExpanded: Boolean) {
        _transactionUiState.value = _transactionUiState.value.copy(isJenisTransaksiExpanded = isExpanded)
    }

    // Update berat emas
    fun updateBeratEmas(beratEmas: String) {
        if (beratEmas.all { it.isDigit() || it == '.' } || beratEmas.isEmpty()) {
            _transactionUiState.value =
                _transactionUiState.value.copy(beratEmas = beratEmas, beratError = null)
            calculateTotalHarga()
        } else {
            // Tampilkan pesan error jika berat emas tidak valid
            _transactionUiState.value = _transactionUiState.value.copy(
                beratEmas = beratEmas,
                beratError = "Berat emas harus berupa angka"
            )
        }
    }

    fun updateHargaDasarPerGram(hargaDasar: String) {
        val digitsOnly = hargaDasar.filter(Char::isDigit)
        val hasNonDigit = hargaDasar.any { !it.isDigit() }

        _transactionUiState.value = _transactionUiState.value.copy(
            hargaDasarPerGram = digitsOnly,
            hargaDasarError = if (digitsOnly.isEmpty() && hargaDasar.isNotEmpty() && hasNonDigit) {
                "Harga dasar harus berupa angka"
            } else {
                null
            }
        )

        calculateTotalHarga()
    }

    // Update ongkos
    fun updateOngkos(ongkos: String) {
        val digitsOnly = ongkos.filter(Char::isDigit)
        val hasNonDigit = ongkos.any { !it.isDigit() }

        _transactionUiState.value = _transactionUiState.value.copy(
            ongkos = digitsOnly,
            ongkosError = if (digitsOnly.isEmpty() && ongkos.isNotEmpty() && hasNonDigit) {
                "Ongkos harus berupa angka"
            } else {
                null
            }
        )

        calculateTotalHarga()
    }

    // Hitung total harga
    private fun calculateTotalHarga() {
        val hargaDasar = _transactionUiState.value.hargaDasarPerGram.toDoubleOrNull() ?: 0.0
        val beratEmas = _transactionUiState.value.beratEmas.toDoubleOrNull() ?: 0.0
        val jumlahBarangDiBeli = _transactionUiState.value.jumlahBarang.toIntOrNull() ?: 1

        // Hitung total harga: (harga dasar * berat) * jumlah + ongkos
        val totalHarga = (hargaDasar * beratEmas * jumlahBarangDiBeli)

        _transactionUiState.value = _transactionUiState.value.copy(totalHarga = totalHarga)
    }

    // ===================
    // Search & Autofill
    // ===================
    fun onSearchQueryChange(query: String) {
        _transactionUiState.value = _transactionUiState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            _transactionUiState.value = _transactionUiState.value.copy(
                showSearchResults = false,
                searchResults = emptyList()
            )
            return
        }

        if (!isCurrentlyOnline) {
            _transactionUiState.value = _transactionUiState.value.copy(
                searchResults = emptyList(),
                showSearchResults = false,
                snackbarMessage = "Fitur pencarian stok memerlukan koneksi internet"
            )
        } else {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                try {
                    stockRepository.searchStocks(query).collect { results ->
                        _transactionUiState.value = _transactionUiState.value.copy(
                            searchResults = results,
                            showSearchResults = results.isNotEmpty()
                        )
                    }
                } catch (e: Exception) {
                    _transactionUiState.value = _transactionUiState.value.copy(
                        searchResults = emptyList(),
                        showSearchResults = false,
                        snackbarMessage = "Gagal mencari stok, barang tidak ditemukan"
                    )
                }
            }
        }
    }

    fun setSearchExpanded(expanded: Boolean) {
        _transactionUiState.value = _transactionUiState.value.copy(showSearchResults = expanded)
    }

    fun selectStock(stock: SupabaseStock) {
        // Autofill fields from selected stock
        _transactionUiState.value = _transactionUiState.value.copy(
            selectedStock = stock,
            searchQuery = stock.nama_barang,
            showSearchResults = false
        )

        updateNamaBarang(stock.nama_barang)
        updateJumlahBarang("1")
        updateKadarEmas(stock.kadar_emas)
        updateBeratEmas(stock.berat_emas.toString())
        updateOngkos(stock.ongkos_per_gram.toLong().toString())

        // Set photo if available, and prefetch a local copy when online for offline-first
        try {
            stock.photo_path?.let { url ->
                setPhotoUri(Uri.parse(url))
                if (isCurrentlyOnline && (url.startsWith("http://") || url.startsWith("https://"))) {
                    viewModelScope.launch {
                        val txId = _transactionUiState.value.idTransaksi.ifBlank { "TMP-${stock.id_barang}" }
                        val local = photoUploadRepository.downloadUrlToLocal(url, txId).getOrNull()
                        if (local != null) {
                            setPhotoUri(Uri.parse("file://$local"))
                        }
                    }
                }
            }
        } catch (_: Exception) { /* ignore bad uri */ }

        // Calculate harga dasar per gram using current gold price
        computeAndSetHargaDasar(stock)
    }

    private fun computeAndSetHargaDasar(stock: SupabaseStock) {
        val price = _transactionUiState.value.goldPrice
        if (price != null) {
            val persen = stock.kadar_persen.replace("%", "").toDoubleOrNull() ?: 0.0
            val hargaDasarPerGram = (price * persen / 100)
            updateHargaDasarPerGram(hargaDasarPerGram.toLong().toString())
            calculateTotalHarga()
        } else {
            // Try fetch price then compute
            fetchGoldPrice(onSuccess = {
                computeAndSetHargaDasar(stock)
            })
        }
    }

    fun fetchGoldPrice(onSuccess: (() -> Unit)? = null) {
        if (!isCurrentlyOnline) return
        viewModelScope.launch {
            try {
                val response = goldPriceRepository.getGoldPrice()
                if (response.isSuccessful) {
                    val goldData = response.body()?.data?.firstOrNull()
                    val price = goldData?.sell?.toDouble()
                    if (price != null) {
                        _transactionUiState.value = _transactionUiState.value.copy(goldPrice = price)
                        onSuccess?.invoke()
                    }
                }
            } catch (_: Exception) { /* ignore */ }
        }
    }

    // Simpan transaksi (offline-first)
    fun simpanTransaksi() {
        viewModelScope.launch {
            val currentState = _transactionUiState.value

            // Validasi form
            if (!validateForm(currentState)) {
                return@launch
            }

            // Pre-validate stock availability for online "Jual"
            val qtyToAdjust = currentState.jumlahBarang.toIntOrNull() ?: 1
            val selected = currentState.selectedStock
            if (isCurrentlyOnline && selected != null && currentState.jenisTransaksi == "Jual") {
                if (qtyToAdjust > selected.jumlah_stok) {
                    _transactionUiState.value = currentState.copy(
                        snackbarMessage = "Stok tidak mencukupi. Stok tersedia: ${selected.jumlah_stok}",
                        error = "Stok tidak mencukupi"
                    )
                    return@launch
                }
            }

            _transactionUiState.value = currentState.copy(isSaving = true, error = null)

            try {
                val result = offlineRepository.saveTransaction(
                    idTransaksi = currentState.idTransaksi,
                    namaBarang = currentState.namaBarang,
                    jumlahBarang = currentState.jumlahBarang.toIntOrNull() ?: 1,
                    kadarEmas = currentState.kadarEmas,
                    jenisTransaksi = currentState.jenisTransaksi,
                    beratEmas = currentState.beratEmas.toDoubleOrNull() ?: 0.0,
                    ongkos = currentState.ongkos.toDoubleOrNull() ?: 0.0,
                    hargaDasarPerGram = currentState.hargaDasarPerGram.toDoubleOrNull() ?: 0.0,
                    totalHarga = currentState.totalHarga,
                    photoUri = currentState.photoUri,
                )

                result.fold(
                    onSuccess = { transactionId ->
                        _transactionUiState.value = currentState.copy(
                            isSaving = false,
                            saveSuccess = true,
                            lastSaveWasOnline = currentState.isOnline
                        )

                        // Update stock in Supabase if online and a stock item was selected
                        viewModelScope.launch {
                            if (isCurrentlyOnline && selected != null) {
                                try {
                                    val adjustResult = when (currentState.jenisTransaksi) {
                                        "Jual" -> stockRepository.reduceStock(selected.id_barang, qtyToAdjust)
                                        else -> Result.success(Unit)
                                    }

                                    adjustResult.onFailure { e ->
                                        _transactionUiState.value = _transactionUiState.value.copy(
                                            snackbarMessage = "Transaksi tersimpan, namun stok tidak diperbarui: ${e.message}"
                                        )
                                    }
                                } catch (e: Exception) {
                                    _transactionUiState.value = _transactionUiState.value.copy(
                                        snackbarMessage = "Transaksi tersimpan, namun gagal memperbarui stok"
                                    )
                                }
                            }
                        }
                        updateUnsyncedCount()
                    },
                    onFailure = { exception ->
                        _transactionUiState.value = currentState.copy(
                            isSaving = false,
                            error = "Gagal menyimpan transaksi: ${exception.message}",
                            snackbarMessage = "Gagal menyimpan transaksi"
                        )
                    }
                )
            } catch (e: Exception) {
                _transactionUiState.value = currentState.copy(
                    isSaving = false,
                    error = "Terjadi kesalahan: ${e.message}",
                    snackbarMessage = "Terjadi kesalahan saat menyimpan"
                )
            }
        }
    }

    // Validasi form
    private fun validateForm(state: TransactionUiState): Boolean {
        val errors = mutableListOf<String>()

        if (state.namaBarang.isEmpty()) errors.add("Nama barang tidak boleh kosong")
        if (state.kadarEmas.isEmpty()) errors.add("Kadar emas harus dipilih")
        if (state.jenisTransaksi.isEmpty()) errors.add("Jenis transaksi harus dipilih")
        if (state.beratEmas.isEmpty() || state.beratEmas.toDoubleOrNull() == null || state.beratEmas.toDouble() <= 0) {
            errors.add("Berat emas harus diisi dengan angka yang valid")
        }
        if (state.hargaDasarPerGram.isEmpty() || state.hargaDasarPerGram.toDoubleOrNull() == null || state.hargaDasarPerGram.toDouble() <= 0) {
            errors.add("Harga dasar per gram harus diisi dengan angka yang valid")
        }

        if (errors.isNotEmpty()) {
            _transactionUiState.value = _transactionUiState.value.copy(
                error = errors.first(),
                snackbarMessage = errors.first()
            )
            return false
        }

        return true
    }

    // Called when user dismisses the success dialog
    fun acknowledgeSaveSuccess() {
        _transactionUiState.value = _transactionUiState.value.copy(
            saveSuccess = false,
            lastSaveWasOnline = null
        )
        clearForm()
    }

    // Export to PDF
    fun exportToPdf(openAfterSave: Boolean = false) {
        viewModelScope.launch {
            try {
                val currentState = _transactionUiState.value

                if (!validateForm(currentState)) {
                    return@launch
                }

                _transactionUiState.value = currentState.copy(isLoading = true)

                // Create temporary transaction entity for PDF
                val tempTransaction = TransactionEntity(
                    id = currentState.idTransaksi.ifEmpty { "PREVIEW" },
                    namaBarang = currentState.namaBarang,
                    jumlahBarang = currentState.jumlahBarang.toIntOrNull() ?: 1,
                    kadarEmas = currentState.kadarEmas,
                    jenisTransaksi = currentState.jenisTransaksi,
                    beratEmas = currentState.beratEmas.toDoubleOrNull() ?: 0.0,
                    ongkos = currentState.ongkos.toDoubleOrNull() ?: 0.0,
                    hargaDasarPerGram = currentState.hargaDasarPerGram.toDoubleOrNull() ?: 0.0,
                    totalHarga = currentState.totalHarga,
                    photoPath = currentState.photoUri?.toString(),
                    createdAt = Date(),
                    updatedAt = Date(),
                    isSynced = currentState.isOnline
                )

                val result = pdfGenerator.generateSingleTransactionReceipt(tempTransaction)

                result.fold(
                    onSuccess = { filePath ->
                        _transactionUiState.value = currentState.copy(
                            isLoading = false,
                            snackbarMessage = "PDF berhasil dibuat: $filePath"
                        )

                        // Buka PDF jika diminta
                        if (openAfterSave) {
                            pdfGenerator.openPdfFile(filePath)
                        }
                    },
                    onFailure = { exception ->
                        _transactionUiState.value = currentState.copy(
                            isLoading = false,
                            error = "Gagal membuat PDF: ${exception.message}",
                            snackbarMessage = "Gagal membuat PDF"
                        )
                    }
                )
            } catch (e: Exception) {
                _transactionUiState.value = _transactionUiState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan: ${e.message}",
                    snackbarMessage = "Terjadi kesalahan saat membuat PDF"
                )
            }
        }
    }

    // Manual sync
    fun syncTransactions() {
        viewModelScope.launch {
            if (!isCurrentlyOnline) {
                _transactionUiState.value = _transactionUiState.value.copy(
                    snackbarMessage = "Tidak ada koneksi internet"
                )
                return@launch
            }

            _transactionUiState.value = _transactionUiState.value.copy(isLoading = true)

            val result = offlineRepository.forcSync()

            result.fold(
                onSuccess = {
                    _transactionUiState.value = _transactionUiState.value.copy(
                        isLoading = false,
                        snackbarMessage = "Sinkronisasi berhasil"
                    )
                    updateUnsyncedCount()
                },
                onFailure = { exception ->
                    _transactionUiState.value = _transactionUiState.value.copy(
                        isLoading = false,
                        error = "Gagal sinkronisasi: ${exception.message}",
                        snackbarMessage = "Gagal sinkronisasi"
                    )
                }
            )
        }
    }

    // Format currency untuk tampilan
    fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount).replace("Rp", "Rp ")
    }

    // Update photo dari galeri/kamera
    fun setPhotoUri(uri: Uri?) {
        _transactionUiState.value = _transactionUiState.value.copy(photoUri = uri)
    }

    // Clear form
    fun clearForm() {
        _transactionUiState.value = TransactionUiState(
            isOnline = _transactionUiState.value.isOnline,
            unsyncedCount = _transactionUiState.value.unsyncedCount
        )
    }

    // Clear snackbar message
    fun clearSnackbarMessage() {
        _transactionUiState.value = _transactionUiState.value.copy(snackbarMessage = null)
    }

    // Clear error
    fun clearError() {
        _transactionUiState.value = _transactionUiState.value.copy(error = null)
    }
}
