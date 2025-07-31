package com.example.rahmatmas.data.supabase.db

import kotlinx.serialization.Serializable

@Serializable
data class SupabaseTransaction(
    val id: String,
    val nama_barang: String,
    val jumlah_barang: Int,
    val kadar_emas: String,
    val jenis_transaksi: String,
    val berat_emas: Double,
    val ongkos: Double,
    val harga_dasar_per_gram: Double,
    val total_harga: Double,
    val photo_path: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)