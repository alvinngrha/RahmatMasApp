package com.example.rahmatmas.data.repository

import com.example.rahmatmas.data.supabase.SupabaseModule
import com.example.rahmatmas.data.supabase.db.SupabaseOrder
import com.example.rahmatmas.data.supabase.db.SupabaseOrderItem
import com.example.rahmatmas.data.supabase.db.SupabaseOrderWithItems
import com.example.rahmatmas.notifications.PushNotificationRequest
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull

class OrderRepository {
    private val client = SupabaseModule.client

    suspend fun placeOrder(order: SupabaseOrder, item: SupabaseOrderItem) {
        client.postgrest.rpc(
            "place_order",
            mapOf(
                "p_id" to order.id,
                "p_recipient_name" to order.recipient_name,
                "p_address" to order.address,
                "p_phone" to order.phone,
                "p_shipping_option" to order.shipping_option,
                "p_status" to order.status,
                "p_note" to order.note,
                "p_user_id" to order.user_id // Add user_id parameter
            )
        )

        client.from("orderitems").insert(item)

        // Send notification to all admins (use special admin identifier)
        sendPushNotification(
            userId = "admin_notifications",
            title = "Pesanan Baru",
            body = "Pesanan baru dari ${order.recipient_name}"
        )
    }

    suspend fun getOrderItems(orderId: String): List<SupabaseOrderItem> {
        return client.from("orderitems")
            .select { filter { eq("order_id", orderId) } }
            .decodeList<SupabaseOrderItem>()
    }

    suspend fun getOrdersWithItems(): List<SupabaseOrderWithItems> {
        val orders = getOrders()
        return orders.map { order ->
            val items = getOrderItems(order.id)
            SupabaseOrderWithItems(order, items)
        }
    }

    suspend fun getOrdersWithItemsByUserId(userId: String): List<SupabaseOrderWithItems> {
        val orders = getOrdersByUserId(userId)
        return orders.map { order ->
            val items = getOrderItems(order.id)
            SupabaseOrderWithItems(order, items)
        }
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

        val updatedOrder = getOrderById(id)
        val userId = updatedOrder?.user_id
        if (userId != null) {
            sendPushNotification(
                userId = userId,
                title = "Status Pesanan Diperbarui",
                body = "Status pesanan Anda: $status"
            )
        }
    }

    suspend fun getOrderById(id: String): SupabaseOrder? {
        return client.from("orders")
            .select()
            .decodeList<SupabaseOrder>()
            .find { it.id == id }
    }

    // Get orders by authenticated user ID
    suspend fun getOrdersByUserId(userId: String): List<SupabaseOrder> {
        return client.from("orders")
            .select { filter {
                eq("user_id", userId)
            } }
            .decodeList<SupabaseOrder>()
            .sortedByDescending { it.created_at }
    }

    // Get current user's orders (using Supabase RLS - Row Level Security)
    suspend fun getCurrentUserOrders(): List<SupabaseOrder> {
        return client.from("orders")
            .select()
            .decodeList<SupabaseOrder>()
            .sortedByDescending { it.created_at }
    }

    suspend fun observeNewOrders(): Flow<SupabaseOrder> {
        val channel = client.channel("public:orders")
        channel.subscribe()
        return channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "orders"
        }.mapNotNull { change ->
            change.decodeRecord<SupabaseOrder>()
        }
    }

    suspend fun observeOrderStatus(userId: String): Flow<SupabaseOrder> {
        val channel = client.channel("public:orders")
        channel.subscribe()
        return channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "orders"
        }.mapNotNull { change ->
            change.decodeRecord<SupabaseOrder>()
        }.filter { it.user_id == userId }
    }

    private suspend fun sendPushNotification(userId: String, title: String, body: String) {
        try {
            val payload = PushNotificationRequest(userId = userId, title = title, body = body)
            val result = client.functions.invoke("send-push", body = payload)
            Log.d("PushNotification", "send-push response: $result")
        } catch (e: Exception) {
            // Log the error but don't let it fail the main operation
            Log.e("PushNotification", "Failed to send push notification", e)
        }
    }
}
