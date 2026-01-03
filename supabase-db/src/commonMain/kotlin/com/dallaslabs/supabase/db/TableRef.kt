package com.dallaslabs.supabase.db

import com.dallaslabs.supabase.core.result.SupabaseResult
import com.dallaslabs.supabase.db.query.FilterBuilder
import com.dallaslabs.supabase.db.query.SelectQuery
import com.dallaslabs.supabase.db.query.InsertQuery
import com.dallaslabs.supabase.db.query.UpdateQuery
import com.dallaslabs.supabase.db.query.DeleteQuery
import com.dallaslabs.supabase.db.query.UpsertQuery
import io.github.jan.supabase.postgrest.Postgrest
import kotlin.reflect.KClass

/**
 * Type-safe reference to a database table.
 *
 * Provides a fluent API for database operations.
 *
 * @param T The data class representing the table row
 */
public class TableRef<T : Any> @PublishedApi internal constructor(
    @PublishedApi internal val postgrest: Postgrest,
    @PublishedApi internal val tableName: String,
    @PublishedApi internal val typeClass: KClass<T>
) {
    /**
     * Creates a SELECT query for this table.
     *
     * @param columns The columns to select (default: "*" for all)
     * @return [SelectQuery] for further configuration
     */
    public fun select(columns: String = "*"): SelectQuery<T> {
        return SelectQuery(postgrest, tableName, columns, typeClass)
    }

    /**
     * Creates an INSERT query for a single item.
     *
     * @param item The item to insert
     * @return [InsertQuery] for further configuration
     */
    public inline fun <reified R : T> insert(item: R): InsertQuery<T> {
        val items = listOf(item)
        return InsertQuery(
            insertAction = { postgrest.from(tableName).insert(items) },
            isEmpty = false
        )
    }

    /**
     * Creates an INSERT query for multiple items.
     *
     * @param items The items to insert
     * @return [InsertQuery] for further configuration
     */
    public inline fun <reified R : T> insert(items: List<R>): InsertQuery<T> {
        return InsertQuery(
            insertAction = { postgrest.from(tableName).insert(items) },
            isEmpty = items.isEmpty()
        )
    }

    /**
     * Creates an UPDATE query with full item.
     *
     * @param item The item with updated values
     * @return [UpdateQuery] for further configuration (must add filter)
     */
    public inline fun <reified R : T> update(item: R): UpdateQuery<T> {
        return UpdateQuery(
            updateAction = { filter ->
                postgrest.from(tableName).update(item) {
                    filter?.applyTo(this)
                }
            },
            tableName = tableName
        )
    }

    /**
     * Creates an UPDATE query with partial values.
     *
     * @param values Map of column names to new values
     * @return [UpdateQuery] for further configuration (must add filter)
     */
    public fun update(values: Map<String, Any?>): UpdateQuery<T> {
        return UpdateQuery(
            updateAction = { filter ->
                postgrest.from(tableName).update(values) {
                    filter?.applyTo(this)
                }
            },
            tableName = tableName
        )
    }

    /**
     * Creates a DELETE query.
     *
     * @return [DeleteQuery] for further configuration (must add filter)
     */
    public fun delete(): DeleteQuery<T> {
        return DeleteQuery(postgrest, tableName)
    }

    /**
     * Creates an UPSERT query for a single item.
     * Inserts the item if it doesn't exist, updates it if it does.
     *
     * @param item The item to upsert
     * @param onConflict The column(s) to use for conflict detection
     * @return [UpsertQuery] for further configuration
     */
    public inline fun <reified R : T> upsert(item: R, onConflict: String? = null): UpsertQuery<T> {
        val items = listOf(item)
        return UpsertQuery(
            upsertAction = { ignoreDuplicates ->
                postgrest.from(tableName).upsert(items) {
                    onConflict?.let { this.onConflict = it }
                    this.ignoreDuplicates = ignoreDuplicates
                }
            },
            isEmpty = false
        )
    }

    /**
     * Creates an UPSERT query for multiple items.
     *
     * @param items The items to upsert
     * @param onConflict The column(s) to use for conflict detection
     * @return [UpsertQuery] for further configuration
     */
    public inline fun <reified R : T> upsert(items: List<R>, onConflict: String? = null): UpsertQuery<T> {
        return UpsertQuery(
            upsertAction = { ignoreDuplicates ->
                postgrest.from(tableName).upsert(items) {
                    onConflict?.let { this.onConflict = it }
                    this.ignoreDuplicates = ignoreDuplicates
                }
            },
            isEmpty = items.isEmpty()
        )
    }

    /**
     * Shorthand for select with filter and immediate execution.
     *
     * @param columns The columns to select
     * @param filter The filter to apply
     * @return Result containing the list of items
     */
    public suspend inline fun <reified R : T> select(
        columns: String = "*",
        noinline filter: FilterBuilder.() -> Unit
    ): SupabaseResult<List<R>> {
        return select(columns).filter(filter).execute<R>()
    }

    /**
     * Shorthand for counting rows matching a filter.
     *
     * @param filter The filter to apply
     * @return Result containing the count
     */
    public suspend fun count(filter: FilterBuilder.() -> Unit = {}): SupabaseResult<Long> {
        return select().filter(filter).count()
    }
}
