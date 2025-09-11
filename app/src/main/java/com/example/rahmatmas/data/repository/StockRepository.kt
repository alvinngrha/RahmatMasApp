package com.example.rahmatmas.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.rahmatmas.data.network.NetworkMonitor
import com.example.rahmatmas.data.supabase.SupabaseModule
import com.example.rahmatmas.data.supabase.db.SupabaseStock
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

// Data class for dynamic price calculation
data class StockPriceInfo(
    val hargaDasarPerGram: Double,
    val totalHarga: Double
)

class StockRepository(
    private val networkMonitor: NetworkMonitor,
    private val context: Context
) {

    private val supabaseClient = SupabaseModule.client
    private val photoUploadStockRepository = PhotoUploadRepository(context)

    // Get all stocks from Supabase
    fun getAllStocks(): Flow<List<SupabaseStock>> = flow {
        try {
            val response = supabaseClient.from("stocks")
                .select()
                .decodeList<SupabaseStock>()
            emit(response.sortedByDescending { it.created_at })
        } catch (e: Exception) {
            Log.e("StockRepository", "Error fetching stocks", e)
            emit(emptyList())
        }
    }

    // Search stocks (client-side filtering)
    fun searchStocks(query: String): Flow<List<SupabaseStock>> = flow {
        try {
            val allStocks = supabaseClient.from("stocks")
                .select()
                .decodeList<SupabaseStock>()

            val filteredStocks = if (query.isEmpty()) {
                allStocks
            } else {
                allStocks.filter {
                    it.nama_barang.contains(query, ignoreCase = true) ||
                            it.kadar_emas.contains(query, ignoreCase = true)
                }
            }

            emit(filteredStocks.sortedByDescending { it.created_at })
        } catch (e: Exception) {
            Log.e("StockRepository", "Error searching stocks", e)
            emit(emptyList())
        }
    }

    // Save stock to Supabase (Updated method signature)
    suspend fun saveStock(
        namaBarang: String,
        jumlahStok: Int,
        kadarEmas: String,
        kadarPersen: String,
        beratEmas: Double,
        ongkosPerGram: Double, // Changed from ongkosPerGram
        photoUri: Uri? = null
    ): Result<String> {
        return try {
            val newStockId = "STK-${UUID.randomUUID()}"

            // Handle photo upload if photo exists
            var cloudPhotoUrl: String? = null

            if (photoUri != null) {
                val photoResult = photoUploadStockRepository.uploadPhotoStock(photoUri, newStockId)
                photoResult.fold(
                    onSuccess = { url ->
                        cloudPhotoUrl = url
                        Log.d("StockRepository", "Photo uploaded: $url")
                    },
                    onFailure = { exception ->
                        Log.w("StockRepository", "Photo upload failed", exception)
                        return Result.failure(Exception("Gagal mengupload foto: ${exception.message}"))
                    }
                )
            }

            val stock = SupabaseStock(
                id_barang = newStockId,
                nama_barang = namaBarang,
                jumlah_stok = jumlahStok,
                kadar_emas = kadarEmas,
                kadar_persen = kadarPersen,
                berat_emas = beratEmas,
                ongkos_per_gram = ongkosPerGram, // Changed from ongkos_per_gram
                photo_path = cloudPhotoUrl
                // Removed: harga_dasar_per_gram and total_harga_barang
            )

            supabaseClient.from("stocks").insert(stock)
            Log.d("StockRepository", "Stock saved successfully: $newStockId")

            Result.success(newStockId)
        } catch (e: Exception) {
            Log.e("StockRepository", "Error saving stock", e)
            Result.failure(e)
        }
    }

    // Get stock by ID (client-side filtering)
    suspend fun getStockById(id: String): SupabaseStock? {
        return try {
            val response = supabaseClient.from("stocks")
                .select()
                .decodeList<SupabaseStock>()

            response.find { it.id_barang == id }
        } catch (e: Exception) {
            Log.e("StockRepository", "Error getting stock by id", e)
            null
        }
    }

    // Update stock using upsert
    suspend fun updateStock(stock: SupabaseStock): Result<Unit> {
        return try {
            supabaseClient.from("stocks").upsert(stock)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("StockRepository", "Error updating stock", e)
            Result.failure(e)
        }
    }

    // Delete stock from Supabase
    suspend fun deleteStock(id: String): Result<Unit> {
        return try {
            supabaseClient.from("stocks")
                .delete {
                    filter {
                        eq("id_barang", id)
                    }
                }
            Log.d("StockRepository", "Stock deleted successfully: $id")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("StockRepository", "Error deleting stock", e)
            Result.failure(e)
        }
    }

    // Reduce stock quantity
    suspend fun reduceStock(id: String, quantity: Int): Result<Unit> {
        return try {
            val currentStock = getStockById(id)
            if (currentStock == null) {
                return Result.failure(Exception("Stok tidak ditemukan"))
            }

            val newQuantity = currentStock.jumlah_stok - quantity
            if (newQuantity < 0) {
                return Result.failure(Exception("Stok tidak mencukupi. Stok tersedia: ${currentStock.jumlah_stok}"))
            }

            val updatedStock = currentStock.copy(
                jumlah_stok = newQuantity,
                updated_at = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date())
            )

            updateStock(updatedStock)
        } catch (e: Exception) {
            Log.e("StockRepository", "Error reducing stock", e)
            Result.failure(e)
        }
    }

    // Increase stock quantity
    suspend fun increaseStock(id: String, quantity: Int): Result<Unit> {
        return try {
            val currentStock = getStockById(id)
            if (currentStock == null) {
                return Result.failure(Exception("Stok tidak ditemukan"))
            }

            val newQuantity = currentStock.jumlah_stok + quantity
            val updatedStock = currentStock.copy(
                jumlah_stok = newQuantity,
                updated_at = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date())
            )

            updateStock(updatedStock)
        } catch (e: Exception) {
            Log.e("StockRepository", "Error increasing stock", e)
            Result.failure(e)
        }
    }

    // Check network status
    fun isOnline(): Flow<Boolean> = networkMonitor.isOnline

    // Get stocks with low quantity (bonus feature)
    fun getLowStockItems(threshold: Int = 10): Flow<List<SupabaseStock>> = flow {
        try {
            val response = supabaseClient.from("stocks")
                .select()
                .decodeList<SupabaseStock>()

            val lowStockItems = response.filter { it.jumlah_stok <= threshold }
                .sortedBy { it.jumlah_stok }

            emit(lowStockItems)
        } catch (e: Exception) {
            Log.e("StockRepository", "Error fetching low stock items", e)
            emit(emptyList())
        }
    }
}