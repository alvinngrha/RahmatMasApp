package com.example.rahmatmas.data.supabase.db

import kotlinx.serialization.Serializable

@Serializable
data class SupabaseStock(
    val id_barang: String,
    val nama_barang: String,
    val jumlah_stok: Int,
    val kadar_emas: String,
    val kadar_persen: String,
    val berat_emas: Double,
    val ongkos_per_gram: Double,
    val photo_path: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
) : java.io.Serializable