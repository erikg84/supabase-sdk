package com.dallaslabs.supabase.core

import com.dallaslabs.supabase.core.config.SupabaseConfigBuilder
import com.dallaslabs.supabase.core.config.SupabaseConfiguration
import com.dallaslabs.supabase.core.error.SupabaseException
import com.dallaslabs.supabase.core.internal.SupabaseCoreClientImpl

/**
 * Main entry point for the Supabase Core SDK.
 *
 * Provides two usage patterns:
 *
 * ## Singleton Pattern (Recommended for most apps)
 * ```kotlin
 * // Initialize once at app startup
 * SupabaseCore.initialize {
 *     projectUrl = "https://xxx.supabase.co"
 *     anonKey = "your-key"
 *     auth {
 *         scheme = "com.myapp"
 *         host = "callback"
 *     }
 * }
 *
 * // Access anywhere in the app
 * val postgrest = SupabaseCore.client.postgrest
 * val auth = SupabaseCore.client.auth
 * ```
 *
 * ## Factory Pattern (For multiple clients or testing)
 * ```kotlin
 * val client = SupabaseCore.createClient {
 *     projectUrl = "https://xxx.supabase.co"
 *     anonKey = "your-key"
 * }
 * ```
 */
public object SupabaseCore {

    private var defaultClient: SupabaseCoreClient? = null
    private var isInitializing = false

    /**
     * Creates a new Supabase client with the provided configuration.
     * Each call creates a new independent instance.
     *
     * @param configuration The pre-built configuration
     * @return A new [SupabaseCoreClient] instance
     */
    public fun createClient(configuration: SupabaseConfiguration): SupabaseCoreClient {
        return SupabaseCoreClientImpl(configuration)
    }

    /**
     * Creates a new Supabase client using the DSL builder.
     * Each call creates a new independent instance.
     *
     * @param block Configuration block
     * @return A new [SupabaseCoreClient] instance
     */
    public fun createClient(block: SupabaseConfigBuilder.() -> Unit): SupabaseCoreClient {
        val config = SupabaseConfigBuilder().apply(block).build()
        return createClient(config)
    }

    /**
     * Initializes the default singleton client.
     * Call this once at app startup (e.g., in Application.onCreate() or iOS app init).
     *
     * @param block Configuration block
     * @return The initialized [SupabaseCoreClient]
     * @throws SupabaseException.AlreadyConfigured if already initialized
     */
    public fun initialize(block: SupabaseConfigBuilder.() -> Unit): SupabaseCoreClient {
        if (defaultClient != null) {
            throw SupabaseException.AlreadyConfigured(
                "Default client already initialized. Use createClient() for additional instances, " +
                "or call reset() first to reinitialize."
            )
        }

        if (isInitializing) {
            throw SupabaseException.AlreadyConfigured(
                "Initialization already in progress."
            )
        }

        isInitializing = true
        try {
            val config = SupabaseConfigBuilder().apply(block).build()
            val client = SupabaseCoreClientImpl(config)
            defaultClient = client
            return client
        } finally {
            isInitializing = false
        }
    }

    /**
     * Initializes the default singleton client if not already initialized.
     * Safe to call multiple times - subsequent calls are no-ops.
     *
     * @param block Configuration block (only used on first call)
     * @return The [SupabaseCoreClient]
     */
    public fun initializeIfNeeded(block: SupabaseConfigBuilder.() -> Unit): SupabaseCoreClient {
        if (defaultClient != null) {
            return defaultClient!!
        }

        val config = SupabaseConfigBuilder().apply(block).build()
        val client = SupabaseCoreClientImpl(config)
        defaultClient = client
        return client
    }

    /**
     * Returns the default singleton client.
     *
     * @throws SupabaseException.NotConfigured if not initialized
     */
    public val client: SupabaseCoreClient
        get() = defaultClient ?: throw SupabaseException.NotConfigured(
            "Default client not initialized. Call SupabaseCore.initialize() first."
        )

    /**
     * Returns the default singleton client, or null if not initialized.
     */
    public val clientOrNull: SupabaseCoreClient?
        get() = defaultClient

    /**
     * Whether the default singleton client has been initialized.
     */
    public val isInitialized: Boolean
        get() = defaultClient != null

    /**
     * Resets the default singleton client.
     * Use with caution - existing references will become invalid.
     */
    public suspend fun reset() {
        val clientToClose = defaultClient
        defaultClient = null
        clientToClose?.close()
    }
}
