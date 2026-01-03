package com.dallaslabs.supabase.core.config

import com.dallaslabs.supabase.core.logging.SupabaseLogger

/**
 * Immutable configuration for Supabase client.
 * Created via [SupabaseConfigBuilder].
 *
 * @property projectUrl The Supabase project URL (e.g., "https://xxx.supabase.co")
 * @property anonKey The Supabase anonymous/public key
 * @property authConfiguration Authentication-specific configuration
 * @property enableLogging Whether to enable debug logging
 * @property logger Custom logger implementation (uses default if null and logging enabled)
 */
@ConsistentCopyVisibility
public data class SupabaseConfiguration internal constructor(
    val projectUrl: String,
    val anonKey: String,
    val authConfiguration: AuthConfiguration?,
    val enableLogging: Boolean,
    val logger: SupabaseLogger?
) {
    init {
        require(projectUrl.isNotBlank()) { "Project URL must not be blank" }
        require(anonKey.isNotBlank()) { "Anon key must not be blank" }
        require(projectUrl.startsWith("https://")) { "Project URL must use HTTPS" }
    }

    /**
     * Returns true if auth configuration is provided.
     */
    public val hasAuthConfiguration: Boolean
        get() = authConfiguration != null

    /**
     * Masked anon key for logging (shows first 20 characters).
     */
    internal val maskedAnonKey: String
        get() = if (anonKey.length > 20) "${anonKey.take(20)}..." else anonKey
}
