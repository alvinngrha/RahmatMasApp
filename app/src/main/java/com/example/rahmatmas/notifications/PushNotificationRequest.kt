package com.example.rahmatmas.notifications

import kotlinx.serialization.Serializable

@Serializable
data class PushNotificationRequest(
    val userId: String,
    val title: String,
    val body: String,
    val userType: String? = null
)
