package com.dallaslabs.sdk.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.Flow

/**
 * Authentication wrapper for Supabase Auth
 */
public class SupabaseAuth(private val client: SupabaseClient) {
    
    /**
     * Sign in with email and password
     */
    public suspend fun signInWithEmail(email: String, password: String): Result<UserInfo> {
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(client.auth.currentUserOrNull() ?: throw IllegalStateException("User not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sign up with email and password
     */
    public suspend fun signUpWithEmail(email: String, password: String): Result<UserInfo> {
        return try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(client.auth.currentUserOrNull() ?: throw IllegalStateException("User not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sign out the current user
     */
    public suspend fun signOut(): Result<Unit> {
        return try {
            client.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get the current user
     */
    public fun currentUser(): UserInfo? {
        return client.auth.currentUserOrNull()
    }
    
    /**
     * Observe session status changes
     */
    public fun observeSessionStatus(): Flow<SessionStatus> {
        return client.auth.sessionStatus
    }
    
    /**
     * Check if user is authenticated
     */
    public fun isAuthenticated(): Boolean {
        return client.auth.currentUserOrNull() != null
    }
}
