package com.dallaslabs.supabase.db.query

import com.dallaslabs.supabase.core.error.SupabaseError
import com.dallaslabs.supabase.core.result.SupabaseResult
import io.github.jan.supabase.postgrest.result.PostgrestResult

/**
 * Represents an INSERT query on a table.
 *
 * @param T The data class representing the table row
 */
public class InsertQuery<T : Any> @PublishedApi internal constructor(
    @PublishedApi internal val insertAction: suspend () -> PostgrestResult,
    @PublishedApi internal val isEmpty: Boolean
) {
    /**
     * Executes the insert and returns the inserted items.
     */
    public suspend inline fun <reified R : T> execute(): SupabaseResult<List<R>> {
        return try {
            if (isEmpty) {
                return SupabaseResult.Success(emptyList())
            }

            val result = insertAction()
            SupabaseResult.Success(result.decodeList<R>())
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Database(
                    message = "INSERT failed: ${e.message}",
                    cause = e
                )
            )
        }
    }

    /**
     * Executes the insert and returns the first inserted item.
     */
    public suspend inline fun <reified R : T> executeSingle(): SupabaseResult<R?> {
        return execute<R>().map { it.firstOrNull() }
    }

    /**
     * Executes the insert without returning data.
     */
    public suspend fun executeWithoutReturning(): SupabaseResult<Unit> {
        return try {
            if (isEmpty) {
                return SupabaseResult.Success(Unit)
            }

            insertAction()
            SupabaseResult.Success(Unit)
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Database(
                    message = "INSERT failed: ${e.message}",
                    cause = e
                )
            )
        }
    }
}
