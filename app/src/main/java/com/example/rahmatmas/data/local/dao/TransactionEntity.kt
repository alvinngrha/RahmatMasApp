package com.example.rahmatmas.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val namaBarang: String,
    val jumlahBarang: Int,
    val kadarEmas: String,
    val jenisTransaksi: String,
    val beratEmas: Double,
    val ongkos: Double,
    val hargaDasarPerGram: Double,
    val totalHarga: Double,
    val photoPath: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isSynced: Boolean = false, // Flag untuk menandai apakah sudah disinkronkan
    val isDeleted: Boolean = false // Soft delete
)