package com.dallaslabs.supabase.core.logging

/**
 * Logger interface for the Supabase SDK.
 * Implement this interface to provide custom logging behavior.
 *
 * Example:
 * ```kotlin
 * class AndroidLogger : SupabaseLogger {
 *     override fun debug(tag: String, message: String) {
 *         Log.d(tag, message)
 *     }
 *     // ... other methods
 * }
 * ```
 */
public interface SupabaseLogger {
    /**
     * Logs a debug message.
     */
    public fun debug(tag: String, message: String)

    /**
     * Logs an info message.
     */
    public fun info(tag: String, message: String)

    /**
     * Logs a warning message.
     */
    public fun warn(tag: String, message: String)

    /**
     * Logs an error message with optional throwable.
     */
    public fun error(tag: String, message: String, throwable: Throwable? = null)
}
