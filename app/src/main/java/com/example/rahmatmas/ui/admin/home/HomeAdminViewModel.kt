package com.example.rahmatmas.ui.admin.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.datastore.AdminAuthManager
import com.example.rahmatmas.data.repository.GoldPriceRepository
import com.example.rahmatmas.data.supabase.AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class GoldPriceUiState(
    val buyPrice: String = "Loading...",
    val sellPrice: String = "Loading...",
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeAdminViewModel(
    private val adminAuthManager: AdminAuthManager,
    private val goldPriceRepository: GoldPriceRepository
) : ViewModel() {

    private val _goldPriceState = MutableStateFlow(GoldPriceUiState())
    val goldPriceState: StateFlow<GoldPriceUiState> = _goldPriceState.asStateFlow()

    init {
        fetchGoldPrices()
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
                        // Handle error jika diperlukan
                        // Untuk saat ini, kita tetap panggil onSuccess karena user harus bisa logout
                        onSuccess()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                // Handle exception, tetapi tetap logout
                onSuccess()
            }
        }
    }

    // Satu function untuk fetch semua data harga emas
    fun fetchGoldPrices() {
        viewModelScope.launch {
            _goldPriceState.value = _goldPriceState.value.copy(isLoading = true, error = null)

            try {
                val response = goldPriceRepository.getGoldPrice()
                if (response.isSuccessful) {
                    val goldPriceResponse = response.body()
                    val goldData = goldPriceResponse?.data?.firstOrNull()

                    if (goldData != null) {
                        val buyPrice = goldData.buy?.let {
                            formatCurrency(it.toDouble())
                        } ?: "Data tidak tersedia"

                        val sellPrice = goldData.sell?.let {
                            formatCurrency(it.toDouble())
                        } ?: "Data tidak tersedia"

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
                    buyPrice = "Error",
                    sellPrice = "Error",
                    isLoading = false,
                    error = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }



    fun refreshData() {
        fetchGoldPrices()
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount).replace("Rp", "Rp ")
    }
}