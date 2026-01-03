package com.dallaslabs.supabase.db

import com.dallaslabs.supabase.core.SupabaseCore
import com.dallaslabs.supabase.core.SupabaseCoreClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Database operations wrapper for Supabase Postgrest.
 *
 * Provides type-safe access to database tables with a fluent API.
 *
 * ## Usage with SupabaseCore singleton
 * ```kotlin
 * val db = SupabaseDatabase.getInstance()
 *
 * // Query
 * val users = db.from<User>("users")
 *     .select()
 *     .filter { eq("active", true) }
 *     .execute()
 *
 * // Insert
 * db.from<User>("users")
 *     .insert(newUser)
 *     .execute()
 * ```
 *
 * ## Usage with custom client
 * ```kotlin
 * val client = SupabaseCore.createClient { ... }
 * val db = SupabaseDatabase(client)
 * ```
 */
public class SupabaseDatabase(
    private val client: SupabaseCoreClient
) {
    /**
     * Direct access to the underlying Postgrest instance.
     * Use for advanced operations not covered by this SDK.
     */
    public val postgrest: Postgrest
        get() = client.postgrest

    /**
     * Creates a type-safe reference to a database table.
     *
     * @param T The data class representing the table row
     * @param tableName The name of the table in the database
     * @return A [TableRef] for performing operations on the table
     */
    public inline fun <reified T : Any> from(tableName: String): TableRef<T> {
        return TableRef(postgrest, tableName, T::class)
    }

    public companion object {
        /**
         * Returns a SupabaseDatabase instance using the default SupabaseCore client.
         *
         * @throws com.dallaslabs.supabase.core.error.SupabaseException.NotConfigured
         *         if SupabaseCore is not initialized
         */
        public fun getInstance(): SupabaseDatabase {
            return SupabaseDatabase(SupabaseCore.client)
        }

        /**
         * Returns a SupabaseDatabase instance if SupabaseCore is initialized, null otherwise.
         */
        public fun getInstanceOrNull(): SupabaseDatabase? {
            return SupabaseCore.clientOrNull?.let { SupabaseDatabase(it) }
        }
    }
}

/**
 * Extension property to get a SupabaseDatabase from a SupabaseCoreClient.
 */
public val SupabaseCoreClient.database: SupabaseDatabase
    get() = SupabaseDatabase(this)
