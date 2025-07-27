package com.example.rahmatmas.ui.admin.transactionrecording


import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
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
    val isKadarEmasExpanded: Boolean = false,
    val isJenisTransaksiExpanded: Boolean = false,
    val snackbarMessage: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val beratError: String? = null,
    val hargaDasarError: String? = null,
    val ongkosError: String? = null,
    val jumlahBarangError: String? = null,
    val photoUri: Uri? = null // Tambahkan ini untuk menyimpan URI foto
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


    // Hitung total harga
    private fun calculateTotalHarga() {
        val hargaDasar = _transactionUiState.value.hargaDasarPerGram.toIntOrNull() ?: 0
        val beratEmas = _transactionUiState.value.beratEmas.toDoubleOrNull() ?: 0.0
        val jumlahBarangDiBeli = _transactionUiState.value.jumlahBarang.toIntOrNull() ?: 1


        // Hitung total harga
        val totalHarga = (hargaDasar * beratEmas) * jumlahBarangDiBeli

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

    // Update photo dari galeri/kamera
    fun setPhotoUri(uri: Uri?) {
        _transactionUiState.value = _transactionUiState.value.copy(photoUri = uri)
    }

//    // Clear form
//    fun clearForm() {
//        _transactionUiState.value = TransactionUiState(
//            hargaJualEmasHariIni = _transactionUiState.value.hargaJualEmasHariIni
//        )
//    }
}