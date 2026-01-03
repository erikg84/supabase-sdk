package com.dallaslabs.sdk.supabase.core

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Configuration for Supabase client
 */
public data class SupabaseConfig(
    val url: String,
    val apiKey: String,
    val enablePostgrest: Boolean = true,
    val enableAuth: Boolean = true
)

/**
 * Creates a configured Supabase client
 */
public fun createSupabaseClient(config: SupabaseConfig): SupabaseClient {
    return createSupabaseClient(
        supabaseUrl = config.url,
        supabaseKey = config.apiKey
    ) {
        if (config.enablePostgrest) {
            install(Postgrest)
        }
        if (config.enableAuth) {
            install(Auth)
        }
    }
}
