package com.example.rahmatmas.data.supabase.db

import kotlinx.serialization.Serializable

@Serializable
data class SupabaseOrder(
    val id: String,
    val stock_id: String,
    val stock_name: String,
    val recipient_name: String,
    val address: String,
    val phone: String,
    val note: String? = null,
    val shipping_option: String,
    val status: String,
    val created_at: String? = null
) : java.io.Serializable