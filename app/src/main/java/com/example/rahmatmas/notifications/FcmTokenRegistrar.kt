package com.example.rahmatmas.notifications

import com.example.rahmatmas.data.supabase.SupabaseModule
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FcmTokenRegistrar {

    fun registerForCurrentUser() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) return@OnCompleteListener
            val token = task.result ?: return@OnCompleteListener
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val client = SupabaseModule.client
                    val userId = client.auth.currentSessionOrNull()?.user?.id ?: return@launch
                    client.from("device_tokens").upsert(
                        mapOf(
                            "user_id" to userId,
                            "token" to token,
                            "user_type" to "customer"
                        )
                    )
                } catch (_: Exception) { }
            }
        })
    }

    fun registerForAdminNotifications() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) return@OnCompleteListener
            val token = task.result ?: return@OnCompleteListener
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val client = SupabaseModule.client
                    client.from("device_tokens").upsert(
                        mapOf(
                            "user_id" to "admin_notifications",
                            "token" to token,
                            "user_type" to "admin"
                        )
                    )
                } catch (_: Exception) { }
            }
        })
    }
}
