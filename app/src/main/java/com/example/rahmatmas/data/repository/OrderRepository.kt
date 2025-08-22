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
                "p_shipping_option" to order.shipping_option,
                "p_status" to order.status,
                "p_note" to order.note
            )
        )
    }

    suspend fun getOrders(): List<SupabaseOrder> {
        return client.from("orders").select().decodeList<SupabaseOrder>()
            .sortedByDescending { it.created_at }
    }

    suspend fun getOrdersByStatus(status: String): List<SupabaseOrder> {
        return client.from("orders")
            .select()
            .decodeList<SupabaseOrder>()
            .filter { it.status == status }
            .sortedByDescending { it.created_at }
    }

    suspend fun updateOrderStatus(
        id: String,
        status: String,
        cancelReason: String? = null,
        cancelledBy: String? = null
    ) {
        // Use RPC function instead of direct update
        val params = mutableMapOf<String, String?>(
            "p_order_id" to id,
            "p_status" to status
        )

        if (status == "dibatalkan") {
            params["p_cancel_reason"] = cancelReason
            params["p_cancelled_by"] = cancelledBy
        }

        client.postgrest.rpc("update_order_status", params)
    }

    suspend fun getOrderById(id: String): SupabaseOrder? {
        return client.from("orders")
            .select()
            .decodeList<SupabaseOrder>()
            .find { it.id == id }
    }

    // For customer to view their orders
    suspend fun getOrdersByPhone(phone: String): List<SupabaseOrder> {
        return client.from("orders")
            .select()
            .decodeList<SupabaseOrder>()
            .filter { it.phone == phone }
            .sortedByDescending { it.created_at }
    }
}