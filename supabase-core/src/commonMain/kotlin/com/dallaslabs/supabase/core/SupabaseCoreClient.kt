package com.dallaslabs.supabase.core

import com.dallaslabs.supabase.core.config.SupabaseConfiguration
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Core Supabase client providing access to Postgrest and Auth modules.
 *
 * This is a thin wrapper around the official Supabase client that enforces
 * proper configuration and provides consistent API access.
 *
 * Obtain an instance via [SupabaseCore.createClient] or [SupabaseCore.initialize].
 */
public interface SupabaseCoreClient {
    /**
     * The underlying Supabase client.
     * Use this for advanced operations not covered by this SDK.
     */
    public val rawClient: SupabaseClient

    /**
     * Access to the Postgrest module for database operations.
     */
    public val postgrest: Postgrest

    /**
     * Access to the Auth module for authentication operations.
     */
    public val auth: Auth

    /**
     * The configuration used to create this client.
     */
    public val configuration: SupabaseConfiguration

    /**
     * Whether this client has been properly configured and is ready for use.
     */
    public val isConfigured: Boolean

    /**
     * Closes the client and releases resources.
     * After calling this, the client should not be used.
     */
    public suspend fun close()
}
