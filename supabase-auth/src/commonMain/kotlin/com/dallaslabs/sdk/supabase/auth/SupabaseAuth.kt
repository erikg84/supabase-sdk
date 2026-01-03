package com.dallaslabs.sdk.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.Flow

/**
 * Authentication wrapper for Supabase Auth
 */
public class SupabaseAuth(private val client: SupabaseClient) {
    
    private val auth: Auth
        get() = client.auth
    
    /**
     * Sign in with email and password
     */
    public suspend fun signInWithEmail(email: String, password: String): Result<UserInfo> {
        return try {
            auth.signInWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
                this.email = email
                this.password = password
            }
            Result.success(auth.currentUserOrNull() ?: throw IllegalStateException("User not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sign up with email and password
     */
    public suspend fun signUpWithEmail(email: String, password: String): Result<UserInfo> {
        return try {
            auth.signUpWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
                this.email = email
                this.password = password
            }
            Result.success(auth.currentUserOrNull() ?: throw IllegalStateException("User not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sign out the current user
     */
    public suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get the current user
     */
    public fun currentUser(): UserInfo? {
        return auth.currentUserOrNull()
    }
    
    /**
     * Observe session status changes
     */
    public fun observeSessionStatus(): Flow<SessionStatus> {
        return auth.sessionStatus
    }
    
    /**
     * Check if user is authenticated
     */
    public fun isAuthenticated(): Boolean {
        return auth.currentUserOrNull() != null
    }
}
