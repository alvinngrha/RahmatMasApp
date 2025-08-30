package com.example.rahmatmas.data.supabase.notification

import kotlinx.serialization.Serializable

@Serializable
data class PushNotificationRequest(
    val userId: String,
    val title: String,
    val body: String
)