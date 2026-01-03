package com.dallaslabs.supabase.core.error

/**
 * Exception types for Supabase operations.
 * These can be thrown directly or converted to [SupabaseError] for Result-based handling.
 */
public sealed class SupabaseException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Thrown when the Supabase client is not configured.
     */
    public class NotConfigured(message: String) : SupabaseException(message)

    /**
     * Thrown when attempting to configure an already configured client.
     */
    public class AlreadyConfigured(message: String) : SupabaseException(message)

    /**
     * Thrown for network-related errors.
     */
    public class NetworkError(
        message: String,
        cause: Throwable? = null
    ) : SupabaseException(message, cause)

    /**
     * Thrown for authentication-related errors.
     */
    public class AuthenticationError(
        message: String,
        public val code: String? = null,
        cause: Throwable? = null
    ) : SupabaseException(message, cause)

    /**
     * Thrown for database/Postgrest-related errors.
     */
    public class DatabaseError(
        message: String,
        public val code: String? = null,
        public val hint: String? = null,
        cause: Throwable? = null
    ) : SupabaseException(message, cause)

    /**
     * Converts this exception to a [SupabaseError].
     */
    public fun toError(): SupabaseError = when (this) {
        is NotConfigured -> SupabaseError.Configuration(message, cause)
        is AlreadyConfigured -> SupabaseError.Configuration(message, cause)
        is NetworkError -> SupabaseError.Network(message, cause)
        is AuthenticationError -> SupabaseError.Authentication(message, code, cause)
        is DatabaseError -> SupabaseError.Database(message, code, hint, cause)
    }
}
