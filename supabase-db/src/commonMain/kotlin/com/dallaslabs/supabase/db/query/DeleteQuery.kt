package com.dallaslabs.supabase.db.query

import com.dallaslabs.supabase.core.error.SupabaseError
import com.dallaslabs.supabase.core.result.SupabaseResult
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Represents a DELETE query on a table.
 *
 * **Important:** You must call [filter] before [execute] to prevent deleting all rows.
 *
 * @param T The data class representing the table row
 */
public class DeleteQuery<T : Any> @PublishedApi internal constructor(
    @PublishedApi internal val postgrest: Postgrest,
    @PublishedApi internal val tableName: String
) {
    @PublishedApi internal var filterBuilder: FilterBuilder? = null

    /**
     * Adds filters to determine which rows to delete.
     * **Required:** You must specify a filter to prevent deleting all rows.
     */
    public fun filter(block: FilterBuilder.() -> Unit): DeleteQuery<T> {
        filterBuilder = FilterBuilder().apply(block)
        return this
    }

    /**
     * Executes the delete operation.
     */
    public suspend fun execute(): SupabaseResult<Unit> {
        val filter = filterBuilder
            ?: return SupabaseResult.Failure(
                SupabaseError.Database(
                    message = "DELETE on '$tableName' requires a filter."
                )
            )

        return try {
            postgrest.from(tableName).delete {
                filter.applyTo(this)
            }
            SupabaseResult.Success(Unit)
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Database(
                    message = "DELETE on '$tableName' failed: ${e.message}",
                    cause = e
                )
            )
        }
    }

    /**
     * Executes the delete and returns the deleted items.
     */
    public suspend inline fun <reified R : T> executeReturning(): SupabaseResult<List<R>> {
        val filter = filterBuilder
            ?: return SupabaseResult.Failure(
                SupabaseError.Database(
                    message = "DELETE on '$tableName' requires a filter."
                )
            )

        return try {
            val result = postgrest.from(tableName).delete {
                filter.applyTo(this)
            }
            SupabaseResult.Success(result.decodeList<R>())
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Database(
                    message = "DELETE on '$tableName' failed: ${e.message}",
                    cause = e
                )
            )
        }
    }
}
