package com.dallaslabs.supabase.db.query

import com.dallaslabs.supabase.core.error.SupabaseError
import com.dallaslabs.supabase.core.result.SupabaseResult
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import kotlin.reflect.KClass

/**
 * Represents a SELECT query on a table.
 *
 * @param T The data class representing the table row
 */
public class SelectQuery<T : Any> @PublishedApi internal constructor(
    @PublishedApi internal val postgrest: Postgrest,
    @PublishedApi internal val tableName: String,
    @PublishedApi internal val columns: String,
    @PublishedApi internal val typeClass: KClass<T>
) {
    @PublishedApi internal var filterBuilder: FilterBuilder? = null
    @PublishedApi internal var orderColumn: String? = null
    @PublishedApi internal var orderAscending: Boolean = true
    @PublishedApi internal var limitCount: Long? = null
    @PublishedApi internal var offsetCount: Long? = null
    @PublishedApi internal var rangeFrom: Long? = null
    @PublishedApi internal var rangeTo: Long? = null

    /**
     * Adds filters to the query.
     */
    public fun filter(block: FilterBuilder.() -> Unit): SelectQuery<T> {
        filterBuilder = FilterBuilder().apply(block)
        return this
    }

    /**
     * Orders results by a column.
     */
    public fun order(column: String, ascending: Boolean = true): SelectQuery<T> {
        orderColumn = column
        orderAscending = ascending
        return this
    }

    /**
     * Limits the number of results.
     */
    public fun limit(count: Long): SelectQuery<T> {
        limitCount = count
        return this
    }

    /**
     * Skips a number of results.
     */
    public fun offset(count: Long): SelectQuery<T> {
        offsetCount = count
        return this
    }

    /**
     * Limits the query to a range of rows.
     * Uses zero-based indexing (0 = first row).
     *
     * @param from Starting index (inclusive)
     * @param to Ending index (inclusive)
     * @return This query for chaining
     *
     * @example
     * ```kotlin
     * // Get rows 0-9 (first 10 rows)
     * .range(0, 9)
     *
     * // Get rows 10-19 (second page of 10)
     * .range(10, 19)
     * ```
     */
    public fun range(from: Long, to: Long): SelectQuery<T> {
        rangeFrom = from
        rangeTo = to
        return this
    }

    /**
     * Executes the query and returns a list of results.
     */
    public suspend inline fun <reified R : T> execute(): SupabaseResult<List<R>> {
        return try {
            val result = postgrest.from(tableName).select(Columns.raw(columns)) {
                filterBuilder?.applyTo(this)

                orderColumn?.let { col ->
                    order(col, if (orderAscending) Order.ASCENDING else Order.DESCENDING)
                }

                // Apply range if set, otherwise use limit/offset
                if (rangeFrom != null && rangeTo != null) {
                    range(rangeFrom!!, rangeTo!!)
                } else {
                    limitCount?.let { limit(it) }
                    offsetCount?.let { off ->
                        val lim = limitCount ?: 1000L
                        range(off, off + lim - 1)
                    }
                }
            }.decodeList<R>()

            SupabaseResult.Success(result)
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Database(
                    message = "SELECT from '$tableName' failed: ${e.message}",
                    cause = e
                )
            )
        }
    }

    /**
     * Executes the query and returns exactly one row.
     * Fails if the query returns 0 rows or more than 1 row.
     *
     * Use this when you expect exactly one result (e.g., finding by unique ID).
     *
     * @return Result containing the single row
     * @throws SupabaseError.Database if 0 or multiple rows are returned
     *
     * @example
     * ```kotlin
     * val project = db.from<Project>("projects")
     *     .select()
     *     .filter { eq("id", projectId) }
     *     .single()  // Returns Project, not List<Project>
     * ```
     */
    public suspend inline fun <reified R : T> single(): SupabaseResult<R> {
        return try {
            val result = postgrest.from(tableName).select(Columns.raw(columns)) {
                filterBuilder?.applyTo(this)
                limit(2)  // Fetch 2 to detect multiple results
            }.decodeList<R>()

            when (result.size) {
                0 -> SupabaseResult.Failure(
                    SupabaseError.Database(
                        message = "SELECT from '$tableName' expected exactly 1 row, got 0"
                    )
                )
                1 -> SupabaseResult.Success(result.first())
                else -> SupabaseResult.Failure(
                    SupabaseError.Database(
                        message = "SELECT from '$tableName' expected exactly 1 row, got ${result.size}+"
                    )
                )
            }
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Database(
                    message = "SELECT from '$tableName' failed: ${e.message}",
                    cause = e
                )
            )
        }
    }

    /**
     * Executes the query and returns one row or null.
     * Fails if the query returns more than 1 row.
     *
     * Use this when you expect 0 or 1 result (e.g., optional lookups).
     *
     * @return Result containing the row or null if none found
     * @throws SupabaseError.Database if multiple rows are returned
     *
     * @example
     * ```kotlin
     * val project = db.from<Project>("projects")
     *     .select()
     *     .filter { eq("user_id", userId) }
     *     .maybeSingle()  // Returns Project? (null if not found)
     * ```
     */
    public suspend inline fun <reified R : T> maybeSingle(): SupabaseResult<R?> {
        return try {
            val result = postgrest.from(tableName).select(Columns.raw(columns)) {
                filterBuilder?.applyTo(this)
                limit(2)  // Fetch 2 to detect multiple results
            }.decodeList<R>()

            when (result.size) {
                0 -> SupabaseResult.Success(null)
                1 -> SupabaseResult.Success(result.first())
                else -> SupabaseResult.Failure(
                    SupabaseError.Database(
                        message = "SELECT from '$tableName' expected 0 or 1 row, got ${result.size}+"
                    )
                )
            }
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Database(
                    message = "SELECT from '$tableName' failed: ${e.message}",
                    cause = e
                )
            )
        }
    }

    /**
     * Executes the query and returns a single result.
     * @deprecated Use single() or maybeSingle() instead for clearer semantics
     */
    @Deprecated(
        message = "Use single() for exactly 1 row, or maybeSingle() for 0 or 1 row",
        replaceWith = ReplaceWith("maybeSingle()"),
        level = DeprecationLevel.WARNING
    )
    public suspend inline fun <reified R : T> executeSingle(): SupabaseResult<R?> {
        return maybeSingle()
    }

    /**
     * Executes the query and returns the count of matching rows.
     */
    public suspend fun count(): SupabaseResult<Long> {
        return try {
            val result = postgrest.from(tableName).select(Columns.raw(columns)) {
                filterBuilder?.applyTo(this)
                count(Count.EXACT)
            }
            SupabaseResult.Success(result.countOrNull() ?: 0L)
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Database(
                    message = "COUNT on '$tableName' failed: ${e.message}",
                    cause = e
                )
            )
        }
    }
}
