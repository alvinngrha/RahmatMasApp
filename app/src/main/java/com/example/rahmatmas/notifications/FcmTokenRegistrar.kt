package com.example.rahmatmas.notifications

import android.util.Log
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
                    client.from("device_tokens").insert(
                        mapOf(
                            "user_id" to userId,
                            "token" to token,
                            "user_type" to "customer"
                        )
                    )
                    Log.d("FcmTokenRegistrar", "Registered customer token for $userId")
                } catch (e: Exception) {
                    Log.e("FcmTokenRegistrar", "Failed to register customer token", e)
                }
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
                    client.from("device_tokens").insert(
                        mapOf(
                            "user_id" to "admin_notifications",
                            "token" to token,
                            "user_type" to "admin"
                        )
                    )
                    Log.d("FcmTokenRegistrar", "Registered admin token")
                } catch (e: Exception) {
                    // RLS might block anon insert; surface in logs so it can be fixed
                    Log.e("FcmTokenRegistrar", "Failed to register admin token (check RLS policy on device_tokens)", e)
                }
            }
        })
    }
}
