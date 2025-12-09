package com.example.rahmatmas.ui.customer.checkout

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahmatmas.data.repository.GoldPriceRepository
import com.example.rahmatmas.data.repository.OrderRepository
import com.example.rahmatmas.data.supabase.SupabaseModule
import com.example.rahmatmas.data.supabase.db.SupabaseOrder
import com.example.rahmatmas.data.supabase.db.SupabaseOrderItem
import com.example.rahmatmas.data.supabase.db.SupabaseStock
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

data class CheckoutUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successOrderId: String? = null,
    val userName: String = "",
    val userEmail: String = "",
    val processingOrderId: String? = null
)

class CheckoutViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val supabaseClient = SupabaseModule.client
    private val goldPriceRepository = GoldPriceRepository()

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val currentUser = supabaseClient.auth.currentUserOrNull()
        if (currentUser != null) {
            _uiState.value = _uiState.value.copy(
                userName = currentUser.userMetadata?.get("full_name")?.toString()
                    ?: currentUser.userMetadata?.get("name")?.toString()
                    ?: "",
                userEmail = currentUser.email ?: ""
            )
        }
    }

    fun getCurrentUserEmail(): String? {
        return try {
            supabaseClient.auth.currentUserOrNull()?.email
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUserName(): String? {
        return try {
            val user = supabaseClient.auth.currentUserOrNull()
            user?.userMetadata?.get("full_name")?.toString()
                ?: user?.userMetadata?.get("name")?.toString()
        } catch (e: Exception) {
            null
        }
    }

    fun placeOrder(
        stock: SupabaseStock,
        quantity: Int,
        name: String,
        address: String,
        phone: String,
        note: String?,
        shippingOption: String
    ) {
        viewModelScope.launch {
            // Prevent duplicate submissions if an order is already being processed
            if (_uiState.value.processingOrderId != null) {
                Log.w("CheckoutViewModel", "Order already being processed, ignoring duplicate request")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Get current user
                val currentUser = supabaseClient.auth.currentUserOrNull()
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Anda harus login terlebih dahulu"
                    )
                    return@launch
                }

                // Validate inputs
                if (name.isBlank() || address.isBlank() || phone.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Semua field wajib diisi (kecuali catatan)"
                    )
                    return@launch
                }

                if (phone.length < 10) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Nomor HP tidak valid (minimal 10 digit)"
                    )
                    return@launch
                }

                val orderId = "ORD-${UUID.randomUUID()}"
                Log.d("CheckoutViewModel", "Creating order with ID: $orderId")

                // Set the processing ID to prevent duplicates
                _uiState.value = _uiState.value.copy(processingOrderId = orderId)

                val currentTime = Instant.now().toString()

                val order = SupabaseOrder(
                    id = orderId,
                    user_id = currentUser.id, // Use authenticated user ID
                    recipient_name = name.trim(),
                    address = address.trim(),
                    phone = phone.trim(),
                    note = note?.trim()?.takeIf { it.isNotBlank() },
                    shipping_option = shippingOption,
                    status = "menunggu konfirmasi",
                    created_at = currentTime,
                    updated_at = currentTime,
                    cancel_reason = null,
                    cancelled_by = null
                )

                Log.d("CheckoutViewModel", "Fetching gold price...")
                val goldPriceResponse = goldPriceRepository.getGoldPrice()
                val goldMetal = goldPriceRepository.extractGoldMetalPrice(goldPriceResponse.body())
                val hargaEmas = goldMetal?.ask
                    ?: goldMetal?.price
                    ?: goldMetal?.price24k
                    ?: 0.0

                if (hargaEmas <= 0) {
                    throw Exception("Tidak dapat memperoleh harga emas hari ini")
                }

                val kadarPersen = stock.kadar_persen.replace("%", "").toDoubleOrNull() ?: 0.0
                val hargaDasarPerGram = (hargaEmas * kadarPersen / 100)
                val validQty = if (quantity < 1) 1 else quantity
                val totalHarga = hargaDasarPerGram * stock.berat_emas * validQty

                Log.d("CheckoutViewModel", "Calculated total price: $totalHarga")

                val orderItem = SupabaseOrderItem(
                    orderitem_id = "ITEM-${UUID.randomUUID()}",
                    order_id = orderId,
                    id_stock = stock.id_barang,
                    nama_stock = stock.nama_barang,
                    jumlah_order = validQty,
                    kadar_emas = stock.kadar_emas,
                    kadar_persen = stock.kadar_persen,
                    berat_emas = stock.berat_emas,
                    ongkos_per_gram = stock.ongkos_per_gram,
                    harga_emas_hariini = hargaEmas,
                    total_harga = totalHarga,
                    photo_path = stock.photo_path
                )

                orderRepository.placeOrder(order, orderItem)

                Log.i(
                    "CheckoutFlow",
                    "Checkout pesanan $orderId selesai, total $totalHarga"
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    successOrderId = orderId,
                    processingOrderId = null // Reset after success
                )
            } catch (e: Exception) {
                Log.e("CheckoutFlow", "Checkout pesanan gagal: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal memproses pesanan: ${e.message}",
                    processingOrderId = null // Reset after failure
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = CheckoutUiState(
            userName = _uiState.value.userName,
            userEmail = _uiState.value.userEmail
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
