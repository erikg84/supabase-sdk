package com.dallaslabs.supabase.db.query

import com.dallaslabs.supabase.core.error.SupabaseError
import com.dallaslabs.supabase.core.result.SupabaseResult
import io.github.jan.supabase.postgrest.result.PostgrestResult

/**
 * Represents an UPDATE query on a table.
 *
 * **Important:** You must call [filter] before [execute] to prevent updating all rows.
 *
 * @param T The data class representing the table row
 */
public class UpdateQuery<T : Any> @PublishedApi internal constructor(
    @PublishedApi internal val updateAction: suspend (FilterBuilder?) -> PostgrestResult,
    @PublishedApi internal val tableName: String
) {
    @PublishedApi internal var filterBuilder: FilterBuilder? = null

    /**
     * Adds filters to determine which rows to update.
     * **Required:** You must specify a filter to prevent updating all rows.
     */
    public fun filter(block: FilterBuilder.() -> Unit): UpdateQuery<T> {
        filterBuilder = FilterBuilder().apply(block)
        return this
    }

    /**
     * Executes the update and returns the updated items.
     */
    public suspend inline fun <reified R : T> execute(): SupabaseResult<List<R>> {
        val filter = filterBuilder
            ?: return SupabaseResult.Failure(
                SupabaseError.Database(
                    message = "UPDATE on '$tableName' requires a filter."
                )
            )

        return try {
            val result = updateAction(filter)
            SupabaseResult.Success(result.decodeList<R>())
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Database(
                    message = "UPDATE on '$tableName' failed: ${e.message}",
                    cause = e
                )
            )
        }
    }

    /**
     * Executes the update and returns the first updated item.
     */
    public suspend inline fun <reified R : T> executeSingle(): SupabaseResult<R?> {
        return execute<R>().map { it.firstOrNull() }
    }

    /**
     * Executes the update without returning data.
     */
    public suspend fun executeWithoutReturning(): SupabaseResult<Unit> {
        val filter = filterBuilder
            ?: return SupabaseResult.Failure(
                SupabaseError.Database(
                    message = "UPDATE on '$tableName' requires a filter."
                )
            )

        return try {
            updateAction(filter)
            SupabaseResult.Success(Unit)
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Database(
                    message = "UPDATE on '$tableName' failed: ${e.message}",
                    cause = e
                )
            )
        }
    }
}
