package com.example.rahmatmas.ui.customer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.repository.GoldPriceRepository
import com.example.rahmatmas.data.supabase.authgoogle.AuthManager
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class HomeUiState(
    val user: UserInfo? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class GoldPriceUiStateCustomer(
    val buyPrice: String = "Loading...",
    val sellPrice: String = "Loading...",
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeCustomerViewModel(private val goldPriceRepository: GoldPriceRepository) : ViewModel() {
    private val authManager = AuthManager()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _goldPriceStateCustomer = MutableStateFlow(GoldPriceUiStateCustomer())
    val goldPriceStateCustomer: StateFlow<GoldPriceUiStateCustomer> = _goldPriceStateCustomer.asStateFlow()

    init {
        loadUserData()
        fetchGoldPrices()
    }

    private fun loadUserData() {
        _uiState.value = _uiState.value.copy(
            user = authManager.getCurrentUser(),
            isLoading = false
        )
    }

    // Satu function untuk fetch semua data harga emas
    fun fetchGoldPrices() {
        viewModelScope.launch {
            _goldPriceStateCustomer.value = _goldPriceStateCustomer.value.copy(isLoading = true, error = null)

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

                        _goldPriceStateCustomer.value = GoldPriceUiStateCustomer(
                            buyPrice = buyPrice,
                            sellPrice = sellPrice,
                            isLoading = false,
                            error = null,
                        )
                    } else {
                        _goldPriceStateCustomer.value = GoldPriceUiStateCustomer(
                            buyPrice = "Data tidak tersedia",
                            sellPrice = "Data tidak tersedia",
                            isLoading = false,
                            error = "Data emas tidak ditemukan"
                        )
                    }
                } else {
                    _goldPriceStateCustomer.value = GoldPriceUiStateCustomer(
                        buyPrice = "Gagal memuat",
                        sellPrice = "Gagal memuat",
                        isLoading = false,
                        error = "Gagal mengambil data: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _goldPriceStateCustomer.value = GoldPriceUiStateCustomer(
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

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            authManager.signOut()
            _uiState.value = _uiState.value.copy(
                user = null,
                isLoading = false
            )
            onSuccess()
        }
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount).replace("Rp", "Rp ")
    }
}