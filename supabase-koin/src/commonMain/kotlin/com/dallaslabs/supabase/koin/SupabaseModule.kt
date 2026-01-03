package com.dallaslabs.supabase.koin

import com.dallaslabs.supabase.auth.SupabaseAuth
import com.dallaslabs.supabase.core.SupabaseCore
import com.dallaslabs.supabase.core.SupabaseCoreClient
import com.dallaslabs.supabase.core.config.SupabaseConfigBuilder
import com.dallaslabs.supabase.db.SupabaseDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Configuration for Supabase SDK Koin integration.
 *
 * @property projectUrl The Supabase project URL
 * @property anonKey The Supabase anonymous key
 * @property authScheme Deep link scheme for auth callbacks
 * @property authHost Deep link host for auth callbacks
 */
public data class SupabaseKoinConfig(
    val projectUrl: String,
    val anonKey: String,
    val authScheme: String? = null,
    val authHost: String? = null
)

/**
 * Creates a Koin module that provides Supabase SDK dependencies.
 *
 * This module provides:
 * - [SupabaseCoreClient] - The core Supabase client
 * - [SupabaseAuth] - Authentication wrapper
 * - [SupabaseDatabase] - Database operations wrapper
 *
 * ## Usage
 * ```kotlin
 * // In your Koin initialization
 * startKoin {
 *     modules(
 *         supabaseModule(
 *             projectUrl = "https://your-project.supabase.co",
 *             anonKey = "your-anon-key",
 *             authScheme = "com.yourapp",
 *             authHost = "login-callback"
 *         ),
 *         // ... other modules
 *     )
 * }
 *
 * // Then inject wherever needed
 * class MyRepository(private val db: SupabaseDatabase) {
 *     suspend fun getItems() = db.from<Item>("items").select().execute()
 * }
 * ```
 *
 * @param projectUrl The Supabase project URL
 * @param anonKey The Supabase anonymous key
 * @param authScheme Deep link scheme for auth callbacks (optional)
 * @param authHost Deep link host for auth callbacks (optional)
 * @return A Koin [Module] with Supabase dependencies
 */
public fun supabaseModule(
    projectUrl: String,
    anonKey: String,
    authScheme: String? = null,
    authHost: String? = null
): Module = module {
    single<SupabaseCoreClient> {
        SupabaseCore.initialize {
            this.projectUrl = projectUrl
            this.anonKey = anonKey
            if (authScheme != null || authHost != null) {
                auth {
                    authScheme?.let { this.scheme = it }
                    authHost?.let { this.host = it }
                }
            }
        }
    }

    single<SupabaseAuth> {
        SupabaseAuth(get())
    }

    single<SupabaseDatabase> {
        SupabaseDatabase(get())
    }
}

/**
 * Creates a Koin module that provides Supabase SDK dependencies.
 *
 * @param config The Supabase configuration
 * @return A Koin [Module] with Supabase dependencies
 */
public fun supabaseModule(config: SupabaseKoinConfig): Module = supabaseModule(
    projectUrl = config.projectUrl,
    anonKey = config.anonKey,
    authScheme = config.authScheme,
    authHost = config.authHost
)

/**
 * Creates a Koin module that provides Supabase SDK dependencies using DSL configuration.
 *
 * ## Usage
 * ```kotlin
 * startKoin {
 *     modules(
 *         supabaseModule {
 *             projectUrl = "https://your-project.supabase.co"
 *             anonKey = "your-anon-key"
 *             auth {
 *                 scheme = "com.yourapp"
 *                 host = "login-callback"
 *             }
 *         }
 *     )
 * }
 * ```
 *
 * @param block Configuration block for Supabase
 * @return A Koin [Module] with Supabase dependencies
 */
public fun supabaseModule(block: SupabaseConfigBuilder.() -> Unit): Module = module {
    single<SupabaseCoreClient> {
        SupabaseCore.initialize(block)
    }

    single<SupabaseAuth> {
        SupabaseAuth(get())
    }

    single<SupabaseDatabase> {
        SupabaseDatabase(get())
    }
}

/**
 * Alternative module that uses the singleton SupabaseCore client.
 *
 * Use this when you've already initialized SupabaseCore elsewhere
 * and want to register the existing instance with Koin.
 *
 * ## Usage
 * ```kotlin
 * // First initialize SupabaseCore
 * SupabaseCore.initialize {
 *     projectUrl = "..."
 *     anonKey = "..."
 * }
 *
 * // Then use the singleton module
 * startKoin {
 *     modules(supabaseSingletonModule())
 * }
 * ```
 */
public fun supabaseSingletonModule(): Module = module {
    single<SupabaseCoreClient> {
        SupabaseCore.client
    }

    single<SupabaseAuth> {
        SupabaseAuth.getInstance()
    }

    single<SupabaseDatabase> {
        SupabaseDatabase.getInstance()
    }
}
