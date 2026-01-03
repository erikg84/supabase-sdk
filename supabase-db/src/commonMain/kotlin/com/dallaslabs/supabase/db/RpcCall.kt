package com.dallaslabs.supabase.db

import com.dallaslabs.supabase.core.error.SupabaseError
import com.dallaslabs.supabase.core.result.SupabaseResult
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.rpc

/**
 * Executes an RPC function and returns a single result.
 *
 * @param T The return type of the function
 * @param P The parameter type (must be @Serializable)
 * @param functionName The name of the RPC function
 * @param parameters Optional parameters to pass to the function
 * @return Result containing the function's return value
 */
public suspend inline fun <reified T : Any, reified P : Any> Postgrest.rpcSingle(
    functionName: String,
    parameters: P? = null
): SupabaseResult<T> {
    return try {
        val result = if (parameters != null) {
            rpc(functionName, parameters).decodeSingle<T>()
        } else {
            rpc(functionName).decodeSingle<T>()
        }
        SupabaseResult.Success(result)
    } catch (e: Exception) {
        SupabaseResult.Failure(
            SupabaseError.Database(
                message = "RPC call '$functionName' failed: ${e.message}",
                cause = e
            )
        )
    }
}

/**
 * Executes an RPC function and returns a list of results.
 *
 * @param T The return type of the function
 * @param P The parameter type (must be @Serializable)
 * @param functionName The name of the RPC function
 * @param parameters Optional parameters to pass to the function
 * @return Result containing the list of return values
 */
public suspend inline fun <reified T : Any, reified P : Any> Postgrest.rpcList(
    functionName: String,
    parameters: P? = null
): SupabaseResult<List<T>> {
    println("[SupabaseDB] rpcList called - function: $functionName, params: $parameters")
    return try {
        println("[SupabaseDB] rpcList - calling rpc...")
        val result = if (parameters != null) {
            rpc(functionName, parameters).decodeList<T>()
        } else {
            rpc(functionName).decodeList<T>()
        }
        println("[SupabaseDB] rpcList - SUCCESS: ${result.size} items returned")
        SupabaseResult.Success(result)
    } catch (e: Exception) {
        println("[SupabaseDB] rpcList - EXCEPTION: ${e::class.simpleName}: ${e.message}")
        println("[SupabaseDB] rpcList - stacktrace: ${e.stackTraceToString()}")
        SupabaseResult.Failure(
            SupabaseError.Database(
                message = "RPC call '$functionName' failed: ${e.message}",
                cause = e
            )
        )
    }
}

/**
 * Executes an RPC function without expecting a return value.
 *
 * @param P The parameter type (must be @Serializable)
 * @param functionName The name of the RPC function
 * @param parameters Optional parameters to pass to the function
 * @return Result indicating success or failure
 */
public suspend inline fun <reified P : Any> Postgrest.rpcExecute(
    functionName: String,
    parameters: P? = null
): SupabaseResult<Unit> {
    return try {
        if (parameters != null) {
            rpc(functionName, parameters)
        } else {
            rpc(functionName)
        }
        SupabaseResult.Success(Unit)
    } catch (e: Exception) {
        SupabaseResult.Failure(
            SupabaseError.Database(
                message = "RPC call '$functionName' failed: ${e.message}",
                cause = e
            )
        )
    }
}

/**
 * Executes an RPC function without parameters and returns a list of results.
 *
 * @param T The return type of the function
 * @param functionName The name of the RPC function
 * @return Result containing the list of return values
 */
public suspend inline fun <reified T : Any> Postgrest.rpcListNoParams(
    functionName: String
): SupabaseResult<List<T>> {
    return try {
        val result = rpc(functionName).decodeList<T>()
        SupabaseResult.Success(result)
    } catch (e: Exception) {
        SupabaseResult.Failure(
            SupabaseError.Database(
                message = "RPC call '$functionName' failed: ${e.message}",
                cause = e
            )
        )
    }
}
