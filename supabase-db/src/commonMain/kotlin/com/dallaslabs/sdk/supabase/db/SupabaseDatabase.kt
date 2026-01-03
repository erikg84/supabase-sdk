package com.dallaslabs.sdk.supabase.db

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder

/**
 * Database operations wrapper for Supabase PostgREST
 */
public class SupabaseDatabase(private val client: SupabaseClient) {
    
    /**
     * Access a table for querying
     */
    public fun from(table: String): PostgrestQueryBuilder {
        return client.postgrest.from(table)
    }
}

/**
 * Filter builder helper functions
 */
public object FilterBuilder {
    // Filter builder functionality is provided by the PostgREST query builder
    // Users can use the fluent API directly on PostgrestQueryBuilder
}
