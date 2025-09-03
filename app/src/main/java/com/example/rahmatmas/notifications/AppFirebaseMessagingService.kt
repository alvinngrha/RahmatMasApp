package com.example.rahmatmas.notifications

import android.os.Build
import com.example.rahmatmas.data.supabase.SupabaseModule
import com.example.rahmatmas.notifications.NotificationUtils.ensureChannel
import com.example.rahmatmas.notifications.NotificationUtils.showNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = SupabaseModule.client
                val userId = client.auth.currentSessionOrNull()?.user?.id
                if (userId != null) {
                    // Ensure uniqueness: remove any existing row for this (user_type, token)
                    client.from("device_tokens").delete {
                        filter {
                            eq("user_type", "customer")
                            eq("token", token)
                        }
                    }
                    client.from("device_tokens").insert(
                        mapOf(
                            "user_id" to userId,
                            "token" to token,
                            "user_type" to "customer"
                        )
                    )
                }
            } catch (_: Exception) {
                // Swallow errors to avoid crashing the service
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Show a notification when app is in foreground
        val title = message.notification?.title ?: message.data["title"] ?: "RahmatMas"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        if (title.isNotBlank() || body.isNotBlank()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ensureChannel(applicationContext)
            }
            showNotification(applicationContext, title, body)
        }
    }
}
