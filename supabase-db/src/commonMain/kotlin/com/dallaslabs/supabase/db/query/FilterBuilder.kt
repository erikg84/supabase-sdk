package com.dallaslabs.supabase.db.query

import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder

/**
 * DSL marker for filter builders.
 */
@DslMarker
public annotation class FilterDsl

/**
 * Sealed class representing filter operations that can be applied.
 */
@PublishedApi
internal sealed class FilterOp {
    data class Eq(val column: String, val value: Any?) : FilterOp()
    data class Neq(val column: String, val value: Any?) : FilterOp()
    data class Gt(val column: String, val value: Any) : FilterOp()
    data class Gte(val column: String, val value: Any) : FilterOp()
    data class Lt(val column: String, val value: Any) : FilterOp()
    data class Lte(val column: String, val value: Any) : FilterOp()
    data class Like(val column: String, val pattern: String) : FilterOp()
    data class Ilike(val column: String, val pattern: String) : FilterOp()
    data class Exact(val column: String, val value: Any?) : FilterOp()
    data class InList(val column: String, val values: List<Any>) : FilterOp()
}

/**
 * Builder for constructing Postgrest filters using string column names.
 *
 * ## Example
 * ```kotlin
 * db.from<User>("users")
 *     .select()
 *     .filter {
 *         eq("status", "active")
 *         gte("age", 18)
 *     }
 *     .execute()
 * ```
 */
@FilterDsl
public class FilterBuilder @PublishedApi internal constructor() {

    @PublishedApi
    internal val operations: MutableList<FilterOp> = mutableListOf()

    /** Equals filter: column = value */
    public fun eq(column: String, value: Any?) {
        operations.add(FilterOp.Eq(column, value))
    }

    /** Not equals filter: column != value */
    public fun neq(column: String, value: Any?) {
        operations.add(FilterOp.Neq(column, value))
    }

    /** Greater than filter: column > value */
    public fun gt(column: String, value: Any) {
        operations.add(FilterOp.Gt(column, value))
    }

    /** Greater than or equal filter: column >= value */
    public fun gte(column: String, value: Any) {
        operations.add(FilterOp.Gte(column, value))
    }

    /** Less than filter: column < value */
    public fun lt(column: String, value: Any) {
        operations.add(FilterOp.Lt(column, value))
    }

    /** Less than or equal filter: column <= value */
    public fun lte(column: String, value: Any) {
        operations.add(FilterOp.Lte(column, value))
    }

    /** LIKE filter (case-sensitive pattern match) */
    public fun like(column: String, pattern: String) {
        operations.add(FilterOp.Like(column, pattern))
    }

    /** ILIKE filter (case-insensitive pattern match) */
    public fun ilike(column: String, pattern: String) {
        operations.add(FilterOp.Ilike(column, pattern))
    }

    /** IS filter for exact matching (null, true, false) */
    public fun exact(column: String, value: Any?) {
        operations.add(FilterOp.Exact(column, value))
    }

    /** IS NULL filter */
    public fun isNull(column: String) {
        operations.add(FilterOp.Exact(column, null))
    }

    /** IN filter: column IN (values) */
    public fun inList(column: String, values: List<Any>) {
        operations.add(FilterOp.InList(column, values))
    }

    /**
     * Applies all filter operations to a PostgrestRequestBuilder.
     * This method works in both contexts:
     * - Direct filter application (delete, update)
     * - Nested filter blocks (select)
     */
    @PublishedApi
    internal fun applyTo(builder: PostgrestRequestBuilder) {
        // Apply filters directly to the builder without nesting another filter block
        // The builder is already in the correct context (delete { }, update { }, etc.)
        operations.forEach { op ->
            builder.filter {
                when (op) {
                    is FilterOp.Eq -> eq(op.column, op.value)
                    is FilterOp.Neq -> neq(op.column, op.value)
                    is FilterOp.Gt -> gt(op.column, op.value)
                    is FilterOp.Gte -> gte(op.column, op.value)
                    is FilterOp.Lt -> lt(op.column, op.value)
                    is FilterOp.Lte -> lte(op.column, op.value)
                    is FilterOp.Like -> like(op.column, op.pattern)
                    is FilterOp.Ilike -> ilike(op.column, op.pattern)
                    is FilterOp.Exact -> exact(op.column, op.value)
                    is FilterOp.InList -> contains(op.column, op.values)
                }
            }
        }
    }
}
