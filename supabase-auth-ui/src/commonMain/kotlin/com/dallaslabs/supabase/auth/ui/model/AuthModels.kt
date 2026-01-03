package com.dallaslabs.supabase.auth.ui.model

import kotlinx.coroutines.flow.StateFlow

/**
 * Authentication service interface
 */
public interface AuthService {
    public val currentUser: StateFlow<AuthUser?>
    public suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult
    public suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult
    public suspend fun sendPasswordResetEmail(email: String): AuthResult
    public suspend fun updatePassword(newPassword: String): AuthResult
    public suspend fun signOut(): AuthResult
    public fun isAuthenticated(): Boolean
    public suspend fun deleteAccount(): AuthResult
}

/**
 * Authenticated user model
 */
public data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val avatarUrl: String?
)

/**
 * Authentication result
 */
public sealed class AuthResult {
    public data class Success(val user: AuthUser?) : AuthResult()
    public data class Error(val message: String, val exception: Exception? = null) : AuthResult()
}
