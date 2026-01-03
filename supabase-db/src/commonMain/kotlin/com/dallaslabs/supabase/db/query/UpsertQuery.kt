package com.dallaslabs.supabase.db.query

import com.dallaslabs.supabase.core.error.SupabaseError
import com.dallaslabs.supabase.core.result.SupabaseResult
import io.github.jan.supabase.postgrest.result.PostgrestResult

/**
 * Represents an UPSERT query on a table.
 *
 * Upsert inserts a row if it doesn't exist, or updates it if it does.
 *
 * @param T The data class representing the table row
 */
public class UpsertQuery<T : Any> @PublishedApi internal constructor(
    @PublishedApi internal val upsertAction: suspend (Boolean) -> PostgrestResult,
    @PublishedApi internal val isEmpty: Boolean
) {
    @PublishedApi internal var ignoreDuplicates: Boolean = false

    /**
     * If true, duplicate rows are ignored instead of updated.
     */
    public fun ignoreDuplicates(enabled: Boolean = true): UpsertQuery<T> {
        ignoreDuplicates = enabled
        return this
    }

    /**
     * Executes the upsert and returns the upserted items.
     */
    public suspend inline fun <reified R : T> execute(): SupabaseResult<List<R>> {
        return try {
            if (isEmpty) {
                return SupabaseResult.Success(emptyList())
            }

            val result = upsertAction(ignoreDuplicates)
            SupabaseResult.Success(result.decodeList<R>())
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Database(
                    message = "UPSERT failed: ${e.message}",
                    cause = e
                )
            )
        }
    }

    /**
     * Executes the upsert and returns the first upserted item.
     */
    public suspend inline fun <reified R : T> executeSingle(): SupabaseResult<R?> {
        return execute<R>().map { it.firstOrNull() }
    }

    /**
     * Executes the upsert without returning data.
     */
    public suspend fun executeWithoutReturning(): SupabaseResult<Unit> {
        return try {
            if (isEmpty) {
                return SupabaseResult.Success(Unit)
            }

            upsertAction(ignoreDuplicates)
            SupabaseResult.Success(Unit)
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Database(
                    message = "UPSERT failed: ${e.message}",
                    cause = e
                )
            )
        }
    }
}
