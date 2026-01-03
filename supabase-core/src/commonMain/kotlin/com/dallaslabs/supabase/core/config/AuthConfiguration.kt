package com.dallaslabs.supabase.core.config

/**
 * Authentication-specific configuration for Supabase.
 *
 * @property scheme The URL scheme for deep links (e.g., "com.dallaslabs.myapp")
 * @property host The host for deep links (e.g., "login-callback")
 * @property autoRefreshSession Whether to automatically refresh the session
 * @property persistSession Whether to persist the session across app restarts
 */
public data class AuthConfiguration(
    val scheme: String,
    val host: String,
    val autoRefreshSession: Boolean = true,
    val persistSession: Boolean = true
) {
    /**
     * The full redirect URL for OAuth callbacks.
     * Format: scheme://host (e.g., "com.dallaslabs.myapp://login-callback")
     */
    public val redirectUrl: String
        get() = "$scheme://$host"

    init {
        require(scheme.isNotBlank()) { "Auth scheme must not be blank" }
        require(host.isNotBlank()) { "Auth host must not be blank" }
    }
}
