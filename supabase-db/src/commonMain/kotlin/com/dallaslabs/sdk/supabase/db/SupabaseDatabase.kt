package com.dallaslabs.sdk.supabase.db

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder

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
 * Filter builder extension for PostgrestRequestBuilder
 */
public class FilterBuilder<T : Any>(private val builder: PostgrestRequestBuilder) {
    
    /**
     * Apply equality filter
     */
    public suspend fun eq(column: String, value: Any): FilterBuilder<T> {
        builder.select {
            filter {
                eq(column, value)
            }
        }
        return this
    }
    
    /**
     * Apply not equal filter
     */
    public suspend fun neq(column: String, value: Any): FilterBuilder<T> {
        builder.select {
            filter {
                neq(column, value)
            }
        }
        return this
    }
    
    /**
     * Apply greater than filter
     */
    public suspend fun gt(column: String, value: Any): FilterBuilder<T> {
        builder.select {
            filter {
                gt(column, value)
            }
        }
        return this
    }
    
    /**
     * Apply greater than or equal filter
     */
    public suspend fun gte(column: String, value: Any): FilterBuilder<T> {
        builder.select {
            filter {
                gte(column, value)
            }
        }
        return this
    }
    
    /**
     * Apply less than filter
     */
    public suspend fun lt(column: String, value: Any): FilterBuilder<T> {
        builder.select {
            filter {
                lt(column, value)
            }
        }
        return this
    }
    
    /**
     * Apply less than or equal filter
     */
    public suspend fun lte(column: String, value: Any): FilterBuilder<T> {
        builder.select {
            filter {
                lte(column, value)
            }
        }
        return this
    }
}
