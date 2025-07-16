package com.example.rahmatmas.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseModule {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://awhyvidcoelagcgwkqks.supabase.co", // Ganti dengan URL Supabase Anda
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImF3aHl2aWRjb2VsYWdjZ3drcWtzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTIyMTM0MjUsImV4cCI6MjA2Nzc4OTQyNX0.jVTeO5bBkjcI5vRc_48UpViYLNiOt50TjKRTqQ51Y-c" // Ganti dengan Anon Key Supabase Anda
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
    }
}