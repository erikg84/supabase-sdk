package com.dallaslabs.supabase.core.error

/**
 * Sealed class representing Supabase operation errors.
 * Used with [com.dallaslabs.supabase.core.result.SupabaseResult] for type-safe error handling.
 */
public sealed class SupabaseError {
    /**
     * The error message.
     */
    public abstract val message: String

    /**
     * The underlying cause, if any.
     */
    public abstract val cause: Throwable?

    /**
     * Network-related error (connection, timeout, etc.).
     */
    public data class Network(
        override val message: String,
        override val cause: Throwable? = null
    ) : SupabaseError()

    /**
     * Authentication-related error (invalid credentials, expired token, etc.).
     */
    public data class Authentication(
        override val message: String,
        val code: String? = null,
        override val cause: Throwable? = null
    ) : SupabaseError()

    /**
     * Database/Postgrest-related error.
     */
    public data class Database(
        override val message: String,
        val code: String? = null,
        val hint: String? = null,
        override val cause: Throwable? = null
    ) : SupabaseError()

    /**
     * Configuration-related error (missing or invalid configuration).
     */
    public data class Configuration(
        override val message: String,
        override val cause: Throwable? = null
    ) : SupabaseError()

    /**
     * Unknown or unexpected error.
     */
    public data class Unknown(
        override val message: String,
        override val cause: Throwable? = null
    ) : SupabaseError()
}
