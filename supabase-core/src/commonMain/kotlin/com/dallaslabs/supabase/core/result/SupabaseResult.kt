package com.dallaslabs.supabase.core.result

import com.dallaslabs.supabase.core.error.SupabaseError
import com.dallaslabs.supabase.core.error.SupabaseException

/**
 * Result wrapper for Supabase operations.
 * Provides type-safe success/failure handling aligned with the shared module's Result pattern.
 */
public sealed class SupabaseResult<out T> {

    /**
     * Successful result containing data.
     */
    public data class Success<T>(val data: T) : SupabaseResult<T>()

    /**
     * Failed result containing an error.
     */
    public data class Failure(val error: SupabaseError) : SupabaseResult<Nothing>()

    /**
     * Returns true if this is a successful result.
     */
    public val isSuccess: Boolean
        get() = this is Success

    /**
     * Returns true if this is a failure result.
     */
    public val isFailure: Boolean
        get() = this is Failure

    /**
     * Returns the data if successful, null otherwise.
     */
    public fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    /**
     * Returns the error if failed, null otherwise.
     */
    public fun errorOrNull(): SupabaseError? = when (this) {
        is Success -> null
        is Failure -> error
    }

    /**
     * Returns the data if successful, throws the error message as exception otherwise.
     */
    public fun getOrThrow(): T = when (this) {
        is Success -> data
        is Failure -> throw Exception(error.message, error.cause)
    }

    /**
     * Transforms the success data using the given function.
     */
    public inline fun <R> map(transform: (T) -> R): SupabaseResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    /**
     * Transforms the success data using a function that returns another SupabaseResult.
     */
    public inline fun <R> flatMap(transform: (T) -> SupabaseResult<R>): SupabaseResult<R> = when (this) {
        is Success -> transform(data)
        is Failure -> this
    }

    /**
     * Performs the given action if successful.
     */
    public inline fun onSuccess(action: (T) -> Unit): SupabaseResult<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Performs the given action if failed.
     */
    public inline fun onFailure(action: (SupabaseError) -> Unit): SupabaseResult<T> {
        if (this is Failure) action(error)
        return this
    }

    /**
     * Folds the result into a single value.
     */
    public inline fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (SupabaseError) -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Failure -> onFailure(error)
    }

    public companion object {
        /**
         * Creates a successful result.
         */
        public fun <T> success(data: T): SupabaseResult<T> = Success(data)

        /**
         * Creates a failure result.
         */
        public fun <T> failure(error: SupabaseError): SupabaseResult<T> = Failure(error)

        /**
         * Wraps a suspending operation and returns a SupabaseResult.
         */
        public suspend inline fun <T> runCatching(
            crossinline block: suspend () -> T
        ): SupabaseResult<T> {
            return try {
                Success(block())
            } catch (e: SupabaseException) {
                Failure(e.toError())
            } catch (e: Exception) {
                Failure(
                    SupabaseError.Unknown(
                        message = e.message ?: "Unknown error occurred",
                        cause = e
                    )
                )
            }
        }
    }
}
