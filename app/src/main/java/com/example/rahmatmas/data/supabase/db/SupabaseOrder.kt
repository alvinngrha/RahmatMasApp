package com.example.rahmatmas.data.supabase.db

import kotlinx.serialization.Serializable

@Serializable
data class SupabaseOrder(
    val id: String,
    val user_id: String?, // User ID from Supabase Auth
    val stock_id: String,
    val stock_name: String,
    val recipient_name: String,
    val address: String,
    val phone: String,
    val note: String? = null,
    val shipping_option: String,
    val status: String,
    val created_at: String? = null,
    val updated_at: String? = null,
    val cancel_reason: String? = null,
    val cancelled_by: String? = null // "admin" or "customer"
) : java.io.Serializable

@Serializable
enum class OrderStatus(val displayName: String) {
    PENDING("Menunggu Konfirmasi"),
    PROCESSING("Sedang Diproses"),
    SHIPPING("Akan Dikirim"),
    COMPLETED("Selesai"),
    CANCELLED("Dibatalkan")
}

fun String.toOrderStatus(): OrderStatus {
    return when (this.lowercase()) {
        "menunggu konfirmasi" -> OrderStatus.PENDING
        "sedang diproses" -> OrderStatus.PROCESSING
        "akan dikirim" -> OrderStatus.SHIPPING
        "selesai" -> OrderStatus.COMPLETED
        "dibatalkan" -> OrderStatus.CANCELLED
        else -> OrderStatus.PENDING
    }
}

fun OrderStatus.toDbString(): String {
    return when (this) {
        OrderStatus.PENDING -> "menunggu konfirmasi"
        OrderStatus.PROCESSING -> "sedang diproses"
        OrderStatus.SHIPPING -> "akan dikirim"
        OrderStatus.COMPLETED -> "selesai"
        OrderStatus.CANCELLED -> "dibatalkan"
    }
}