package com.example.rahmatmas.data.repository

import com.example.rahmatmas.data.supabase.SupabaseModule
import com.example.rahmatmas.data.supabase.db.SupabaseOrder
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

class OrderRepository {
    private val client = SupabaseModule.client

    suspend fun placeOrder(order: SupabaseOrder) {
        client.postgrest.rpc(
            "place_order",
            mapOf(
                "p_id" to order.id,
                "p_stock_id" to order.stock_id,
                "p_stock_name" to order.stock_name,
                "p_recipient_name" to order.recipient_name,
                "p_address" to order.address,
                "p_phone" to order.phone,
                "p_note" to order.note,
                "p_shipping_option" to order.shipping_option,
                "p_status" to order.status
            )
        )
    }

    suspend fun getOrders(): List<SupabaseOrder> {
        return client.from("orders").select().decodeList()
    }

    suspend fun updateOrderStatus(id: String, status: String) {
        client.from("orders").update({
            set("status", status)
        }) {
            filter { eq("id", id) }
        }
    }
}