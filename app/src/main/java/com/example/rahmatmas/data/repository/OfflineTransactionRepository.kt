package com.example.rahmatmas.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.rahmatmas.data.local.dao.TransactionDao
import com.example.rahmatmas.data.local.dao.TransactionEntity
import com.example.rahmatmas.data.network.NetworkMonitor
import com.example.rahmatmas.data.supabase.SupabaseModule
import com.example.rahmatmas.data.supabase.db.SupabaseTransaction
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID


class OfflineTransactionRepository(
    private val transactionDao: TransactionDao,
    private val networkMonitor: NetworkMonitor,
    private val context: Context
) {

    private val supabaseClient = SupabaseModule.client
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val photoUploadRepository = PhotoUploadRepository(context)

    // Current network status
    private var isCurrentlyOnline = false

    init {
        // Monitor network changes and sync when online
        networkMonitor.isOnline
            .onEach { isOnline ->
                isCurrentlyOnline = isOnline
                if (isOnline) {
                    syncTransactions()
                }
            }
            .launchIn(coroutineScope)
    }

    // Get all transactions
    fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    // Save transaction locally
    suspend fun saveTransaction(
        idTransaksi: String,
        namaBarang: String,
        jumlahBarang: Int,
        kadarEmas: String,
        jenisTransaksi: String,
        beratEmas: Double,
        ongkos: Double,
        hargaDasarPerGram: Double,
        totalHarga: Double,
        photoUri: Uri? = null
    ): Result<String> {
        return try {
            val newTransactionId = "RB-${UUID.randomUUID()}"

            // Handle photo upload if photo exists
            var localPhotoPath: String? = null
            var cloudPhotoUrl: String? = null

            if (photoUri != null) {
                val photoResult = photoUploadRepository.uploadAndSavePhoto(
                    photoUri = photoUri,
                    transactionId = newTransactionId,
                    isOnline = isCurrentlyOnline
                )

                photoResult.fold(
                    onSuccess = { result ->
                        localPhotoPath = result.localPath
                        cloudPhotoUrl = result.cloudUrl
                        Log.d("OfflineTransactionRepo", "Photo uploaded - Local: $localPhotoPath, Cloud: $cloudPhotoUrl")
                    },
                    onFailure = { exception ->
                        Log.w("OfflineTransactionRepo", "Photo upload failed, continuing without photo", exception)
                        // Continue without photo rather than failing the whole transaction
                    }
                )
            }
            val transaction = TransactionEntity(
                id = newTransactionId,
                namaBarang = namaBarang,
                jumlahBarang = jumlahBarang,
                kadarEmas = kadarEmas,
                jenisTransaksi = jenisTransaksi,
                beratEmas = beratEmas,
                ongkos = ongkos,
                hargaDasarPerGram = hargaDasarPerGram,
                totalHarga = totalHarga,
                photoPath = localPhotoPath,
                createdAt = Date(),
                updatedAt = Date(),
                isSynced = false,
                isDeleted = false
            )

            transactionDao.insertTransaction(transaction)

            // Try to sync immediately if online
            coroutineScope.launch {
                if (isCurrentlyOnline) {
                    syncSingleTransaction(transaction, cloudPhotoUrl)
                }
            }

            Result.success(transaction.id)
        } catch (e: Exception) {
            Log.e("OfflineTransactionRepo", "Error saving transaction", e)
            Result.failure(e)
        }
    }

    // Get transaction by ID
    suspend fun getTransactionById(id: String): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }

    // Update transaction
    suspend fun updateTransaction(transaction: TransactionEntity): Result<Unit> {
        return try {
            val updatedTransaction = transaction.copy(
                updatedAt = Date(),
                isSynced = false
            )
            transactionDao.updateTransaction(updatedTransaction)

            // Try to sync immediately if online
            coroutineScope.launch {
                if (isCurrentlyOnline) {
                    syncSingleTransaction(updatedTransaction)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("OfflineTransactionRepo", "Error updating transaction", e)
            Result.failure(e)
        }
    }

    // Delete transaction (soft delete)
    suspend fun deleteTransaction(id: String): Result<Unit> {
        return try {
            transactionDao.softDeleteTransaction(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("OfflineTransactionRepo", "Error deleting transaction", e)
            Result.failure(e)
        }
    }

    // Get unsynced count
    suspend fun getUnsyncedCount(): Int {
        return transactionDao.getUnsyncedCount()
    }



    // Sync all unsynced transactions
    suspend fun syncTransactions(): Result<Unit> {
        return try {
            val unsyncedTransactions = transactionDao.getUnsyncedTransactions()
            if (unsyncedTransactions.isEmpty()) {
                return Result.success(Unit)
            }
            Log.d("OfflineTransactionRepo", "Syncing ${unsyncedTransactions.size} transactions")

            for (entity in unsyncedTransactions) {

                // Try to upload photo if exists and not already uploaded
                var cloudPhotoUrl: String? = null
                if (!entity.photoPath.isNullOrEmpty()) {
                    if (entity.photoPath.startsWith("http")) {
                        // Already a cloud URL (e.g., from stock/order item)
                        cloudPhotoUrl = entity.photoPath
                    } else {
                        try {
                            val photoUri = Uri.parse("file://${entity.photoPath}")
                            val photoResult = photoUploadRepository.uploadPhoto(photoUri, entity.id)
                            cloudPhotoUrl = photoResult.getOrNull()
                        } catch (e: Exception) {
                            Log.w("OfflineTransactionRepo", "Failed to upload photo for transaction ${entity.id}", e)
                            // Continue without photo URL
                        }
                    }
                }

                val supabaseTransaction = SupabaseTransaction(
                    id = entity.id,
                    nama_barang = entity.namaBarang,
                    jumlah_barang = entity.jumlahBarang,
                    kadar_emas = entity.kadarEmas,
                    jenis_transaksi = entity.jenisTransaksi,
                    berat_emas = entity.beratEmas,
                    ongkos = entity.ongkos,
                    harga_dasar_per_gram = entity.hargaDasarPerGram,
                    total_harga = entity.totalHarga,
                    photo_path = cloudPhotoUrl,
                    // ...created_at, updated_at...
                )
                supabaseClient.from("transactions").insert(supabaseTransaction)
                // Update Room: mark as synced
                transactionDao.updateTransaction(entity.copy(isSynced = true))
            }

            Log.d("OfflineTransactionRepo", "Successfully synced transactions")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("OfflineTransactionRepo", "Error syncing transactions", e)
            Result.failure(e)
        }
    }

    // Sync single transaction
    private suspend fun syncSingleTransaction(transaction: TransactionEntity, cloudPhotoUrl: String? = null): Result<Unit> {
        return try {

            // Use provided cloudPhotoUrl or try to upload photo if exists
            var finalPhotoUrl = cloudPhotoUrl
            if (finalPhotoUrl == null && !transaction.photoPath.isNullOrEmpty()) {
                if (transaction.photoPath.startsWith("http")) {
                    finalPhotoUrl = transaction.photoPath
                } else {
                    try {
                        val photoUri = Uri.parse("file://${transaction.photoPath}")
                        val photoResult = photoUploadRepository.uploadPhoto(photoUri, transaction.id)
                        finalPhotoUrl = photoResult.getOrNull()
                    } catch (e: Exception) {
                        Log.w("OfflineTransactionRepo", "Failed to upload photo for single transaction", e)
                    }
                }
            }

            val supabaseTransaction = SupabaseTransaction(
                id = transaction.id,
                nama_barang = transaction.namaBarang,
                jumlah_barang = transaction.jumlahBarang,
                kadar_emas = transaction.kadarEmas,
                jenis_transaksi = transaction.jenisTransaksi,
                berat_emas = transaction.beratEmas,
                ongkos = transaction.ongkos,
                harga_dasar_per_gram = transaction.hargaDasarPerGram,
                total_harga = transaction.totalHarga,
                photo_path = finalPhotoUrl,
                // ...created_at, updated_at...
            )
            supabaseClient.from("transactions").insert(supabaseTransaction)
            transactionDao.updateTransaction(transaction.copy(isSynced = true))
            Log.d("OfflineTransactionRepo", "Successfully synced single transaction: ${transaction.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("OfflineTransactionRepo", "Error syncing single transaction", e)
            Result.failure(e)
        }
    }

    // Get transactions by date range for PDF export
    suspend fun getTransactionsByDateRange(startDate: Date, endDate: Date): List<TransactionEntity> {
        return transactionDao.getTransactionsByDateRange(startDate.time, endDate.time)
    }

    // Force sync (manual sync)
    suspend fun forcSync(): Result<Unit> {
        return syncTransactions()
    }

    // Save transaction directly from order item (with existing cloud photo URL)
    suspend fun saveTransactionFromOrderItem(
        idTransaksi: String,
        namaBarang: String,
        jumlahBarang: Int,
        kadarEmas: String,
        jenisTransaksi: String,
        beratEmas: Double,
        ongkos: Double,
        hargaDasarPerGram: Double,
        totalHarga: Double,
        photoUrl: String?
    ): Result<String> {
        return try {
            val newTransactionId = idTransaksi.ifBlank { "RB-${UUID.randomUUID()}" }
            val transaction = TransactionEntity(
                id = newTransactionId,
                namaBarang = namaBarang,
                jumlahBarang = jumlahBarang,
                kadarEmas = kadarEmas,
                jenisTransaksi = jenisTransaksi,
                beratEmas = beratEmas,
                ongkos = ongkos,
                hargaDasarPerGram = hargaDasarPerGram,
                totalHarga = totalHarga,
                photoPath = photoUrl,
                createdAt = Date(),
                updatedAt = Date(),
                isSynced = false,
                isDeleted = false
            )
            transactionDao.insertTransaction(transaction)
            // Try sync immediately if online
            coroutineScope.launch {
                if (isCurrentlyOnline) {
                    syncSingleTransaction(transaction, cloudPhotoUrl = photoUrl)
                }
            }
            Result.success(transaction.id)
        } catch (e: Exception) {
            Log.e("OfflineTransactionRepo", "Error saving transaction from order item", e)
            Result.failure(e)
        }
    }
}
