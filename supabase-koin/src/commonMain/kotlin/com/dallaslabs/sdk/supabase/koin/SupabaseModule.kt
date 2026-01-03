package com.dallaslabs.sdk.supabase.koin

import com.dallaslabs.sdk.supabase.auth.SupabaseAuth
import com.dallaslabs.sdk.supabase.core.SupabaseConfig
import com.dallaslabs.sdk.supabase.core.createSupabaseClient
import com.dallaslabs.sdk.supabase.db.SupabaseDatabase
import io.github.jan.supabase.SupabaseClient
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Creates a Koin module for Supabase SDK
 */
public fun supabaseModule(config: SupabaseConfig): Module = module {
    single<SupabaseClient> {
        createSupabaseClient(config)
    }
    
    single<SupabaseDatabase> {
        SupabaseDatabase(get())
    }
    
    single<SupabaseAuth> {
        SupabaseAuth(get())
    }
}

/**
 * Creates a Koin module for Supabase SDK with URL and API key
 */
public fun supabaseModule(url: String, apiKey: String): Module {
    return supabaseModule(
        SupabaseConfig(
            url = url,
            apiKey = apiKey
        )
    )
}
