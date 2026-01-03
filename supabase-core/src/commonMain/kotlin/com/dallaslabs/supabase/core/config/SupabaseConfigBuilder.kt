package com.dallaslabs.supabase.core.config

import com.dallaslabs.supabase.core.logging.DefaultLogger
import com.dallaslabs.supabase.core.logging.SupabaseLogger

/**
 * DSL marker for Supabase configuration builders.
 */
@DslMarker
public annotation class SupabaseDsl

/**
 * Builder for creating [SupabaseConfiguration].
 *
 * Usage:
 * ```kotlin
 * val config = supabaseConfig {
 *     projectUrl = "https://xxx.supabase.co"
 *     anonKey = "your-anon-key"
 *
 *     auth {
 *         scheme = "com.yourapp"
 *         host = "login-callback"
 *     }
 *
 *     logging {
 *         enabled = true
 *         logger = CustomLogger()
 *     }
 * }
 * ```
 */
@SupabaseDsl
public class SupabaseConfigBuilder {
    /**
     * The Supabase project URL (required).
     * Example: "https://xxx.supabase.co"
     */
    public var projectUrl: String = ""

    /**
     * The Supabase anonymous/public key (required).
     */
    public var anonKey: String = ""

    private var authConfig: AuthConfiguration? = null
    private var loggingEnabled: Boolean = false
    private var customLogger: SupabaseLogger? = null

    /**
     * Configures authentication settings.
     */
    @SupabaseDsl
    public fun auth(block: AuthConfigBuilder.() -> Unit) {
        authConfig = AuthConfigBuilder().apply(block).build()
    }

    /**
     * Configures logging settings.
     */
    @SupabaseDsl
    public fun logging(block: LoggingConfigBuilder.() -> Unit) {
        val builder = LoggingConfigBuilder().apply(block)
        loggingEnabled = builder.enabled
        customLogger = builder.logger
    }

    internal fun build(): SupabaseConfiguration = SupabaseConfiguration(
        projectUrl = projectUrl,
        anonKey = anonKey,
        authConfiguration = authConfig,
        enableLogging = loggingEnabled,
        logger = if (loggingEnabled) customLogger ?: DefaultLogger() else null
    )
}

/**
 * Builder for authentication configuration.
 */
@SupabaseDsl
public class AuthConfigBuilder {
    /**
     * The URL scheme for deep links (required).
     * Example: "com.dallaslabs.myapp"
     */
    public var scheme: String = ""

    /**
     * The host for deep links.
     * Default: "callback"
     */
    public var host: String = "callback"

    /**
     * Whether to automatically refresh the session.
     * Default: true
     */
    public var autoRefreshSession: Boolean = true

    /**
     * Whether to persist the session across app restarts.
     * Default: true
     */
    public var persistSession: Boolean = true

    internal fun build(): AuthConfiguration = AuthConfiguration(
        scheme = scheme,
        host = host,
        autoRefreshSession = autoRefreshSession,
        persistSession = persistSession
    )
}

/**
 * Builder for logging configuration.
 */
@SupabaseDsl
public class LoggingConfigBuilder {
    /**
     * Whether logging is enabled.
     * Default: false
     */
    public var enabled: Boolean = false

    /**
     * Custom logger implementation.
     * If null and logging is enabled, uses [DefaultLogger].
     */
    public var logger: SupabaseLogger? = null
}

/**
 * Creates a [SupabaseConfiguration] using a DSL builder.
 *
 * @param block Configuration block
 * @return Configured [SupabaseConfiguration]
 */
public fun supabaseConfig(block: SupabaseConfigBuilder.() -> Unit): SupabaseConfiguration {
    return SupabaseConfigBuilder().apply(block).build()
}
