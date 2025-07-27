package com.example.rahmatmas.ui.admin.transactionrecording

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.util.Locale

data class TransactionUiState(
    val idTransaksi: String = "",
    val namaBarang: String = "",
    val kadarEmas: String = "",
    val beratEmas: String = "",
    val ongkos: String = "",
    val hargaDasarPerGram: String = "",
    val totalHarga: Double = 0.0,
    val isKadarEmasExpanded: Boolean = false,
    val snackbarMessage: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val beratError: String? = null,
    val hargaDasarError: String? = null,
    val ongkosError: String? = null,
)

class TransactionRecordingViewModel : ViewModel() {

    private val _transactionUiState = MutableStateFlow(TransactionUiState())
    val transactionUiState: StateFlow<TransactionUiState> = _transactionUiState.asStateFlow()

    //update id transaksi
    fun updateIdTransaksi(idTransaksi: String) {
        _transactionUiState.value = _transactionUiState.value.copy(idTransaksi = idTransaksi)
    }

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
    fun updateBeratEmas(beratEmas: String) {
        if (beratEmas.all { it.isDigit() } || beratEmas.isEmpty()) {
            _transactionUiState.value =
                _transactionUiState.value.copy(beratEmas = beratEmas, beratError = null)
            calculateTotalHarga()
        } else {
            // Tampilkan pesan error jika id transaksi tidak valid
            _transactionUiState.value = _transactionUiState.value.copy(
                beratEmas = beratEmas,
                beratError = "berat emas harus berupa angka"
            )
        }
    }

    fun updateHargaDasarPerGram(hargaDasar: String) {
        if (hargaDasar.all { it.isDigit() } || hargaDasar.isEmpty()) {
            _transactionUiState.value =
                _transactionUiState.value.copy(hargaDasarPerGram = hargaDasar, hargaDasarError = null)

            // Hitung ulang total harga jika berat emas sudah diisi
            if (_transactionUiState.value.beratEmas.isNotEmpty()) {
                calculateTotalHarga()
            } else {
                _transactionUiState.value = _transactionUiState.value.copy(totalHarga = 0.0)
            }
        } else {
            // Tampilkan pesan error jika harga dasar tidak valid
            _transactionUiState.value = _transactionUiState.value.copy(
                hargaDasarPerGram = hargaDasar,
                hargaDasarError = "Harga dasar harus berupa angka"
            )
        }
    }

    // Update ongkos
    fun updateOngkos(ongkos: String) {
        if (ongkos.all { it.isDigit() } || ongkos.isEmpty()) {
            _transactionUiState.value = _transactionUiState.value.copy(ongkos = ongkos, ongkosError = null)
        } else {
            // Tampilkan pesan error jika ongkos tidak valid
            _transactionUiState.value = _transactionUiState.value.copy(
                ongkos = ongkos,
                ongkosError = "Ongkos harus berupa angka"
            )
        }
    }

    fun updateKadarEmasExpanded(isExpanded: Boolean) {
        _transactionUiState.value = _transactionUiState.value.copy(isKadarEmasExpanded = isExpanded)
    }

    // Hitung total harga
    private fun calculateTotalHarga() {
        val hargaDasar = _transactionUiState.value.hargaDasarPerGram.toIntOrNull() ?: 0
        val beratEmas = _transactionUiState.value.beratEmas.toDoubleOrNull() ?: 0.0


        // Hitung total harga
        val totalHarga = hargaDasar * beratEmas

        _transactionUiState.value = _transactionUiState.value.copy(totalHarga = totalHarga)
    }


    fun hitungTotal() {
        calculateTotalHarga()
    }

    fun simpanTransaksi() {
        val currentState = _transactionUiState.value
        println("Menyimpan transaksi:")
        println("Nama Barang: ${currentState.namaBarang}")
        println("Kadar Emas: ${currentState.kadarEmas}")
        println("Berat Emas: ${currentState.beratEmas} gram")
        println("Ongkos: ${formatCurrency(currentState.ongkos.toDoubleOrNull() ?: 0.0)}")
        println("Harga Dasar per Gram: ${formatCurrency(currentState.hargaDasarPerGram.toDoubleOrNull() ?: 0.0)}")
        println("Total Harga: ${formatCurrency(currentState.totalHarga)}")
    }

    // Format currency untuk tampilan
    fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount).replace("Rp", "Rp ")
    }

//    // Clear form
//    fun clearForm() {
//        _transactionUiState.value = TransactionUiState(
//            hargaJualEmasHariIni = _transactionUiState.value.hargaJualEmasHariIni
//        )
//    }
}