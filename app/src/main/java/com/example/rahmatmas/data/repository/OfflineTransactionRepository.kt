package com.example.rahmatmas.data.repository

import android.content.Context
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
        photoPath: String? = null
    ): Result<String> {
        return try {
            val newTransactionId = "RB-${UUID.randomUUID()}"
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
                photoPath = photoPath,
                createdAt = Date(),
                updatedAt = Date(),
                isSynced = false,
                isDeleted = false
            )

            transactionDao.insertTransaction(transaction)

            // Try to sync immediately if online
            coroutineScope.launch {
                if (isCurrentlyOnline) {
                    syncSingleTransaction(transaction)
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
                // Tidak upload foto, photo_path selalu null
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
                    photo_path = null,
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
    private suspend fun syncSingleTransaction(transaction: TransactionEntity): Result<Unit> {
        return try {
            // Tidak upload foto, photo_path selalu null
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
                photo_path = null,
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
}
