package com.example.rahmatmas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id AND isDeleted = 0")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("UPDATE transactions SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)

    @Query("UPDATE transactions SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteTransaction(id: String)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT COUNT(*) FROM transactions WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("SELECT * FROM transactions WHERE createdAt BETWEEN :startDate AND :endDate AND isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getTransactionsByDateRange(startDate: Long, endDate: Long): List<TransactionEntity>
}