package com.dallaslabs.supabase.core.internal

import com.dallaslabs.supabase.core.SupabaseCoreClient
import com.dallaslabs.supabase.core.config.SupabaseConfiguration
import com.dallaslabs.supabase.core.error.SupabaseException
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

private const val TAG = "SupabaseCore"

internal class SupabaseCoreClientImpl(
    override val configuration: SupabaseConfiguration
) : SupabaseCoreClient {

    private var _client: SupabaseClient? = null

    override val rawClient: SupabaseClient
        get() = _client ?: throw SupabaseException.NotConfigured(
            "Client not initialized. This should not happen - please report this bug."
        )

    override val postgrest: Postgrest
        get() = rawClient.postgrest

    override val auth: Auth
        get() = rawClient.auth

    override val isConfigured: Boolean
        get() = _client != null

    init {
        initialize()
    }

    private fun initialize() {
        configuration.logger?.debug(TAG, "Initializing Supabase client...")
        configuration.logger?.debug(TAG, "Project URL: ${configuration.projectUrl}")
        configuration.logger?.debug(TAG, "Anon key: ${configuration.maskedAnonKey}")

        try {
            _client = createSupabaseClient(
                supabaseUrl = configuration.projectUrl,
                supabaseKey = configuration.anonKey
            ) {
                install(Postgrest)

                if (configuration.hasAuthConfiguration) {
                    val authConfig = configuration.authConfiguration!!
                    install(Auth) {
                        scheme = authConfig.scheme
                        host = authConfig.host
                    }
                    configuration.logger?.debug(TAG, "Auth configured with redirect: ${authConfig.redirectUrl}")
                } else {
                    install(Auth)
                    configuration.logger?.debug(TAG, "Auth installed with default configuration")
                }
            }

            configuration.logger?.info(TAG, "Supabase client initialized successfully")
        } catch (e: Exception) {
            configuration.logger?.error(TAG, "Failed to initialize Supabase client: ${e.message}", e)
            throw SupabaseException.NetworkError(
                message = "Failed to initialize Supabase client: ${e.message}",
                cause = e
            )
        }
    }

    override suspend fun close() {
        configuration.logger?.debug(TAG, "Closing Supabase client...")
        try {
            _client?.close()
            _client = null
            configuration.logger?.info(TAG, "Supabase client closed")
        } catch (e: Exception) {
            configuration.logger?.error(TAG, "Error closing Supabase client: ${e.message}", e)
        }
    }
}
