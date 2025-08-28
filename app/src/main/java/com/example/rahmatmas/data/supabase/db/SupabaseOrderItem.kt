package com.example.rahmatmas.data.supabase.db

import kotlinx.serialization.Serializable

@Serializable
data class SupabaseOrderItem(
    val orderitem_id: String,
    val order_id: String,
    val id_stock: String,
    val nama_stock: String,
    val jumlah_order: Int,
    val kadar_emas: String,
    val kadar_persen: String,
    val berat_emas: Double,
    val ongkos_per_gram: Double,
    val harga_emas_hariini: Double,
    val total_harga: Double
)

data class SupabaseOrderWithItems(
    val order: SupabaseOrder,
    val items: List<SupabaseOrderItem>
) : java.io.Serializable {
    val id: String get() = order.id
    val user_id: String? get() = order.user_id
    val recipient_name: String get() = order.recipient_name
    val address: String get() = order.address
    val phone: String get() = order.phone
    val note: String? get() = order.note
    val shipping_option: String get() = order.shipping_option
    val status: String get() = order.status
    val created_at: String? get() = order.created_at
    val updated_at: String? get() = order.updated_at
    val cancel_reason: String? get() = order.cancel_reason
    val cancelled_by: String? get() = order.cancelled_by
}