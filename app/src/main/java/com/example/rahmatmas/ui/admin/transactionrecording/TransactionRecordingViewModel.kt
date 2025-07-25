package com.example.rahmatmas.ui.admin.transactionrecording

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.util.Locale

data class TransactionUiState(
    val namaBarang: String = "",
    val kadarEmas: String = "",
    val beratEmas: String = "",
    val ongkos: Int = 0,
    val hargaDasarPerGram: Int = 0,
    val totalHarga: Double = 0.0,
    val hargaJualEmasHariIni: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class TransactionRecordingViewModel: ViewModel() {

    private val _transactionUiState = MutableStateFlow(TransactionUiState())
    val transactionUiState: StateFlow<TransactionUiState> = _transactionUiState.asStateFlow()


//    // Fetch harga emas dari API
//    private fun fetchGoldPrice() {
//        viewModelScope.launch {
//            _transactionUiState.value = _transactionUiState.value.copy(isLoading = true, error = null)
//
//            try {
//                val response = goldPriceRepository.getGoldPrice()
//                if (response.isSuccessful) {
//                    val goldData = response.body()?.data?.firstOrNull()
//                    val hargaJual = goldData?.sell?.toDouble() ?: 0.0
//
//                    _transactionUiState.value = _transactionUiState.value.copy(
//                        hargaJualEmasHariIni = hargaJual,
//                        isLoading = false
//                    )
//
//                    // Hitung ulang harga dasar jika kadar sudah dipilih
//                    if (_transactionUiState.value.kadarEmas.isNotEmpty()) {
//                        calculateHargaDasar()
//                    }
//                } else {
//                    _transactionUiState.value = _transactionUiState.value.copy(
//                        isLoading = false,
//                        error = "Gagal mengambil harga emas"
//                    )
//                }
//            } catch (e: Exception) {
//                _transactionUiState.value = _transactionUiState.value.copy(
//                    isLoading = false,
//                    error = e.message ?: "Terjadi kesalahan"
//                )
//            }
//        }
//    }

    // Update nama barang
    fun updateNamaBarang(nama: String) {
        _transactionUiState.value = _transactionUiState.value.copy(namaBarang = nama)
    }

    // Update kadar emas
    fun updateKadarEmas(kadar: String) {
        _transactionUiState.value = _transactionUiState.value.copy(kadarEmas = kadar)
        calculateTotalHarga()
    }

    // Update berat emas
    fun updateBeratEmas(berat: String) {
        _transactionUiState.value = _transactionUiState.value.copy(beratEmas = berat)
        calculateTotalHarga()
    }

    fun updateHargaDasarPerGram(hargaDasar: Int) {
        _transactionUiState.value = _transactionUiState.value.copy(hargaDasarPerGram = hargaDasar)
        // Hitung ulang total harga jika berat emas sudah diisi
        if (_transactionUiState.value.beratEmas.isNotEmpty()) {
            calculateTotalHarga()
        } else {
            _transactionUiState.value = _transactionUiState.value.copy(totalHarga = 0.0)
        }
    }

    // Update ongkos
    fun updateOngkos(ongkos: Int) {
        _transactionUiState.value = _transactionUiState.value.copy(ongkos = ongkos)
        calculateTotalHarga()
    }

    // Hitung total harga
    private fun calculateTotalHarga() {
        val hargaDasar = _transactionUiState.value.hargaDasarPerGram
        val beratText = _transactionUiState.value.beratEmas

        val berat = beratText.toDoubleOrNull() ?: 0.0

        val totalHarga = hargaDasar * berat

        _transactionUiState.value = _transactionUiState.value.copy(totalHarga = totalHarga)
    }


    fun hitungTotal() {
        calculateTotalHarga()
    }

    // Format currency untuk tampilan
    fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount).replace("Rp", "Rp ")
    }

    // Clear form
    fun clearForm() {
        _transactionUiState.value = TransactionUiState(
            hargaJualEmasHariIni = _transactionUiState.value.hargaJualEmasHariIni
        )
    }
}