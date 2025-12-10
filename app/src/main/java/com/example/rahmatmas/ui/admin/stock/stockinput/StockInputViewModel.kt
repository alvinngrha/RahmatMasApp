package com.example.rahmatmas.ui.admin.stock.stockinput

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.network.NetworkMonitor
import com.example.rahmatmas.data.repository.StockRepository
import com.example.rahmatmas.data.supabase.db.SupabaseStock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class StockInputUiState(
    val id_barang: String = "STK-${UUID.randomUUID()}",
    val namaBarang: String = "",
    val jumlahStok: String = "",
    val kadarEmas: String = "",
    val kadarPersen: String = "",
    val beratEmas: String = "",
    val hargaModal: String = "",
    val ongkosPerGram: String = "", // Changed from ongkosPerGram
    val selectedPhotoUri: Uri? = null,
    val existingPhotoUrl: String? = null,
    val isLoading: Boolean = false,
    val isOnline: Boolean = true,
    val showSuccessDialog: Boolean = false,
    val errorMessage: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
    val isEdit: Boolean = false
    // Removed: goldPricePerGram, hargaDasarPerGram, totalHargaBarang
)

class StockInputViewModel(
    private val context: Context
) : ViewModel() {

    private val networkMonitor = NetworkMonitor(context)
    private val stockRepository = StockRepository(
        networkMonitor = networkMonitor,
        context = context
    )
    private val photoUploadRepository = com.example.rahmatmas.data.repository.PhotoUploadRepository(context)

    private val _uiState = MutableStateFlow(StockInputUiState())
    val uiState: StateFlow<StockInputUiState> = _uiState.asStateFlow()

    // Kadar emas options
    val kadarEmasOptions = listOf("700", "833", "999")

    // Kadar persen options based on kadar emas
    val kadarPersenOptions = mapOf(
        "700" to listOf("75%", "77%","78%", "79%", "80%", "81%", "82%", "83%", "84%", "85%"),
        "833" to listOf("85%","87%","88%", "89%", "90%", "91%", "92%"),
        "999" to listOf("100%")
    )

    init {
        // Monitor network status
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.value = _uiState.value.copy(isOnline = isOnline)
                if (!isOnline) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Tidak ada koneksi internet. Fitur ini memerlukan koneksi online."
                    )
                }
            }
        }
    }

    fun updateNamaBarang(value: String) {
        _uiState.value = _uiState.value.copy(namaBarang = value)
        clearValidationError("namaBarang")
    }

    fun updateJumlahStok(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(jumlahStok = value)
            clearValidationError("jumlahStok")
        }
    }

    fun updateKadarEmas(value: String) {
        _uiState.value = _uiState.value.copy(
            kadarEmas = value,
            kadarPersen = "" // Reset kadar persen when kadar emas changes
        )
        clearValidationError("kadarEmas")
    }

    fun updateKadarPersen(value: String) {
        _uiState.value = _uiState.value.copy(kadarPersen = value)
        clearValidationError("kadarPersen")
    }

    fun updateBeratEmas(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.value = _uiState.value.copy(beratEmas = value)
            clearValidationError("beratEmas")
        }
    }

    fun updateHargaModal(value: String) {
        if (value.isEmpty()) {
            _uiState.value = _uiState.value.copy(hargaModal = "")
            clearValidationError("hargaModal")
            return
        }

        val digitsOnly = value.filter(Char::isDigit)
        if (digitsOnly.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(hargaModal = digitsOnly)
            clearValidationError("hargaModal")
        }
    }

    fun updateOngkos(value: String) {
        if (value.isEmpty()) {
            _uiState.value = _uiState.value.copy(ongkosPerGram = "")
            clearValidationError("ongkos")
            return
        }

        val digitsOnly = value.filter(Char::isDigit)
        if (digitsOnly.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(ongkosPerGram = digitsOnly)
            clearValidationError("ongkos")
        }
    }

    fun updateSelectedPhoto(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selectedPhotoUri = uri)
    }

    fun loadForEdit(stock: com.example.rahmatmas.data.supabase.db.SupabaseStock) {
        _uiState.value = _uiState.value.copy(
            id_barang = stock.id_barang,
            namaBarang = stock.nama_barang,
            jumlahStok = stock.jumlah_stok.toString(),
            kadarEmas = stock.kadar_emas,
            kadarPersen = stock.kadar_persen,
            beratEmas = stock.berat_emas.toString(),
            ongkosPerGram = stock.ongkos_per_gram.toLong().toString(),
            hargaModal = stock.harga_modal.toLong().toString(),
            existingPhotoUrl = stock.photo_path,
            isEdit = true
        )
    }

    private fun validateInputs(): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        val currentState = _uiState.value

        // Check network connection first
        if (!currentState.isOnline) {
            errors["network"] = "Tidak ada koneksi internet. Fitur ini memerlukan koneksi online."
            return errors
        }

        if (currentState.namaBarang.isBlank()) {
            errors["namaBarang"] = "Nama barang tidak boleh kosong"
        }

        if (currentState.jumlahStok.isBlank()) {
            errors["jumlahStok"] = "Jumlah stok tidak boleh kosong"
        } else {
            val jumlah = currentState.jumlahStok.toIntOrNull()
            if (jumlah == null || jumlah <= 0) {
                errors["jumlahStok"] = "Jumlah stok harus berupa angka positif"
            }
        }

        if (currentState.kadarEmas.isBlank()) {
            errors["kadarEmas"] = "Pilih kadar emas"
        }

        if (currentState.kadarPersen.isBlank()) {
            errors["kadarPersen"] = "Pilih kadar persen"
        }

        if (currentState.beratEmas.isBlank()) {
            errors["beratEmas"] = "Berat emas tidak boleh kosong"
        } else {
            val berat = currentState.beratEmas.toDoubleOrNull()
            if (berat == null || berat <= 0) {
                errors["beratEmas"] = "Berat emas harus berupa angka positif"
            }
        }

        if (currentState.ongkosPerGram.isBlank()) {
            errors["ongkos"] = "Ongkos tidak boleh kosong"
        } else {
            val ongkos = currentState.ongkosPerGram.toDoubleOrNull()
            if (ongkos == null || ongkos < 0) {
                errors["ongkos"] = "Ongkos harus berupa angka non-negatif"
            }
        }

        // Validasi harga modal
        if (currentState.hargaModal.isBlank()) {
            errors["hargaModal"] = "Harga modal tidak boleh kosong"
        } else {
            val hargaModal = currentState.hargaModal.toDoubleOrNull()
            if (hargaModal == null || hargaModal <= 0) {
                errors["hargaModal"] = "Harga modal harus berupa angka positif"
            }
        }

        return errors
    }

    fun saveStock() {
        viewModelScope.launch {
            if (_uiState.value.isEdit) {
                updateExistingStock()
                return@launch
            }
            val validationErrors = validateInputs()
            if (validationErrors.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(validationErrors = validationErrors)
                // Set error message for network error
                if (validationErrors.containsKey("network")) {
                    _uiState.value = _uiState.value.copy(errorMessage = validationErrors["network"])
                }
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                validationErrors = emptyMap()
            )

            try {
                val currentState = _uiState.value

                val result = stockRepository.saveStock(
                    namaBarang = currentState.namaBarang,
                    jumlahStok = currentState.jumlahStok.toInt(),
                    kadarEmas = currentState.kadarEmas,
                    kadarPersen = currentState.kadarPersen,
                    beratEmas = currentState.beratEmas.toDouble(),
                    ongkosPerGram = currentState.ongkosPerGram.toDouble(), // Changed from ongkosPerGram
                    hargaModal = currentState.hargaModal.toDouble(),
                    photoUri = currentState.selectedPhotoUri
                )

                result.fold(
                    onSuccess = { stockId ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            showSuccessDialog = true
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Gagal menyimpan stok: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Terjadi kesalahan: ${e.message}"
                )
            }
        }
    }

    private suspend fun updateExistingStock() {
        val validationErrors = validateInputs()
        if (validationErrors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(validationErrors = validationErrors)
            if (validationErrors.containsKey("network")) {
                _uiState.value = _uiState.value.copy(errorMessage = validationErrors["network"])
            }
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        val currentState = _uiState.value

        try {
            // Determine photo_path
            var photoUrl: String? = currentState.existingPhotoUrl
            val newPhoto = currentState.selectedPhotoUri
            if (newPhoto != null) {
                val uploadRes = photoUploadRepository.uploadPhotoStock(newPhoto, currentState.id_barang)
                uploadRes.onSuccess { url -> photoUrl = url }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Gagal mengunggah foto: ${e.message}"
                        )
                        return
                    }
            }

            val updated = SupabaseStock(
                id_barang = currentState.id_barang,
                nama_barang = currentState.namaBarang,
                jumlah_stok = currentState.jumlahStok.toInt(),
                kadar_emas = currentState.kadarEmas,
                kadar_persen = currentState.kadarPersen,
                berat_emas = currentState.beratEmas.toDouble(),
                ongkos_per_gram = currentState.ongkosPerGram.toDouble(),
                harga_modal = currentState.hargaModal.toDouble(),
                photo_path = photoUrl,
                updated_at = SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    Locale.getDefault()
                ).format(Date())
            )

            val result = stockRepository.updateStock(updated)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showSuccessDialog = true
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Gagal memperbarui stok: ${e.message}"
                    )
                }
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Terjadi kesalahan: ${e.message}"
            )
        }
    }

    fun resetForm() {
        _uiState.value = StockInputUiState(
            isOnline = _uiState.value.isOnline
        )
    }

    fun dismissSuccessDialog() {
        _uiState.value = _uiState.value.copy(showSuccessDialog = false)
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun clearValidationError(field: String) {
        val currentErrors = _uiState.value.validationErrors.toMutableMap()
        currentErrors.remove(field)
        _uiState.value = _uiState.value.copy(validationErrors = currentErrors)
    }
}
