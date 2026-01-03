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
     * Executes the query and returns a list of results.
     */
    public suspend inline fun <reified R : T> execute(): SupabaseResult<List<R>> {
        return try {
            val result = postgrest.from(tableName).select(Columns.raw(columns)) {
                filterBuilder?.applyTo(this)

                orderColumn?.let { col ->
                    order(col, if (orderAscending) Order.ASCENDING else Order.DESCENDING)
                }

                limitCount?.let { limit(it) }
                offsetCount?.let { off ->
                    val lim = limitCount ?: 1000L
                    range(off, off + lim - 1)
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
     * Executes the query and returns a single result.
     */
    public suspend inline fun <reified R : T> executeSingle(): SupabaseResult<R?> {
        return try {
            val result = postgrest.from(tableName).select(Columns.raw(columns)) {
                filterBuilder?.applyTo(this)
                limit(1)
            }.decodeList<R>()

            SupabaseResult.Success(result.firstOrNull())
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
