package com.dallaslabs.supabase.auth

import com.dallaslabs.supabase.core.SupabaseCore
import com.dallaslabs.supabase.core.SupabaseCoreClient
import com.dallaslabs.supabase.core.error.SupabaseError
import com.dallaslabs.supabase.core.result.SupabaseResult
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Authentication wrapper for Supabase Auth.
 *
 * Provides a simplified, type-safe API for common authentication operations.
 *
 * ## Usage with SupabaseCore singleton
 * ```kotlin
 * val auth = SupabaseAuth.getInstance()
 *
 * // Sign in with email
 * auth.signInWithEmail("user@example.com", "password")
 *     .onSuccess { user -> println("Signed in: ${user.email}") }
 *     .onFailure { error -> println("Error: ${error.message}") }
 *
 * // Observe auth state
 * auth.authState.collect { state ->
 *     when (state) {
 *         is AuthState.Authenticated -> showHome(state.user)
 *         is AuthState.Unauthenticated -> showLogin()
 *         else -> showLoading()
 *     }
 * }
 * ```
 *
 * ## Usage with custom client
 * ```kotlin
 * val client = SupabaseCore.createClient { ... }
 * val auth = SupabaseAuth(client)
 * ```
 */
public class SupabaseAuth(
    private val client: SupabaseCoreClient
) {
    /**
     * Direct access to the underlying Auth instance.
     * Use for advanced operations not covered by this SDK.
     */
    public val rawAuth: Auth
        get() = client.auth

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unknown)

    /**
     * Current authentication state as a StateFlow.
     * Observe this to react to authentication changes.
     */
    public val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Current user if authenticated, null otherwise.
     */
    public val currentUser: AuthUser?
        get() = _authState.value.currentUser

    /**
     * Whether the user is currently authenticated.
     */
    public val isAuthenticated: Boolean
        get() = _authState.value.isAuthenticated

    /**
     * Flow of the current session status from the Supabase SDK.
     */
    public val sessionStatus: Flow<SessionStatus>
        get() = client.auth.sessionStatus

    init {
        refreshAuthState()
    }

    /**
     * Refreshes the current authentication state from the session.
     */
    public fun refreshAuthState() {
        try {
            val session = client.auth.currentSessionOrNull()
            val sessionUser = session?.user
            if (sessionUser != null) {
                val user = AuthUser.fromUserInfo(sessionUser)
                _authState.value = AuthState.Authenticated(user)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to get auth state", e)
        }
    }

    // ========== Email Authentication ==========

    /**
     * Signs in a user with email and password.
     *
     * @param email User's email address
     * @param password User's password
     * @return Result containing the authenticated user or an error
     */
    public suspend fun signInWithEmail(
        email: String,
        password: String
    ): SupabaseResult<AuthUser> {
        _authState.value = AuthState.Loading
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            refreshAuthState()
            val user = currentUser
            if (user != null) {
                SupabaseResult.Success(user)
            } else {
                _authState.value = AuthState.Error("Sign in succeeded but no user returned")
                SupabaseResult.Failure(
                    SupabaseError.Authentication(message = "Sign in succeeded but no user returned")
                )
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Sign in failed", e)
            SupabaseResult.Failure(
                SupabaseError.Authentication(
                    message = e.message ?: "Sign in failed",
                    cause = e
                )
            )
        }
    }

    /**
     * Creates a new user account with email and password.
     *
     * @param email User's email address
     * @param password User's password
     * @param metadata Optional user metadata (display_name, avatar_url, etc.)
     * @return Result containing the created user or an error
     */
    public suspend fun signUpWithEmail(
        email: String,
        password: String,
        metadata: Map<String, Any>? = null
    ): SupabaseResult<AuthUser> {
        _authState.value = AuthState.Loading
        return try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                metadata?.let { meta ->
                    this.data = buildJsonObject {
                        meta.forEach { (key, value) ->
                            put(key, value.toString())
                        }
                    }
                }
            }
            refreshAuthState()
            val user = currentUser
            if (user != null) {
                SupabaseResult.Success(user)
            } else {
                // User created but needs email confirmation
                _authState.value = AuthState.Unauthenticated
                SupabaseResult.Failure(
                    SupabaseError.Authentication(
                        message = "Account created. Please check your email to confirm."
                    )
                )
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Sign up failed", e)
            SupabaseResult.Failure(
                SupabaseError.Authentication(
                    message = e.message ?: "Sign up failed",
                    cause = e
                )
            )
        }
    }

    // ========== OTP Authentication ==========

    /**
     * Signs in or signs up a user with email OTP (magic link).
     *
     * @param email User's email address
     * @param redirectUrl URL to redirect to after OTP verification
     * @return Result indicating success or an error
     */
    public suspend fun signInWithEmailOtp(
        email: String,
        redirectUrl: String? = null
    ): SupabaseResult<Unit> {
        return try {
            client.auth.signInWith(OTP) {
                this.email = email
                redirectUrl?.let { this.createUser = true }
            }
            SupabaseResult.Success(Unit)
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Authentication(
                    message = e.message ?: "Failed to send OTP",
                    cause = e
                )
            )
        }
    }

    /**
     * Verifies an OTP code sent via email.
     *
     * @param email User's email address
     * @param token The OTP token received
     * @return Result containing the authenticated user or an error
     */
    public suspend fun verifyEmailOtp(
        email: String,
        token: String
    ): SupabaseResult<AuthUser> {
        _authState.value = AuthState.Loading
        return try {
            client.auth.verifyEmailOtp(
                type = io.github.jan.supabase.auth.OtpType.Email.EMAIL,
                email = email,
                token = token
            )
            refreshAuthState()
            val user = currentUser
            if (user != null) {
                SupabaseResult.Success(user)
            } else {
                _authState.value = AuthState.Error("OTP verification succeeded but no user returned")
                SupabaseResult.Failure(
                    SupabaseError.Authentication(message = "OTP verification succeeded but no user returned")
                )
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "OTP verification failed", e)
            SupabaseResult.Failure(
                SupabaseError.Authentication(
                    message = e.message ?: "OTP verification failed",
                    cause = e
                )
            )
        }
    }

    /**
     * Signs in or signs up a user with phone OTP.
     *
     * @param phone User's phone number
     * @return Result indicating success or an error
     */
    public suspend fun signInWithPhoneOtp(
        phone: String
    ): SupabaseResult<Unit> {
        return try {
            client.auth.signInWith(OTP) {
                this.phone = phone
            }
            SupabaseResult.Success(Unit)
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Authentication(
                    message = e.message ?: "Failed to send OTP",
                    cause = e
                )
            )
        }
    }

    /**
     * Verifies an OTP code sent via phone.
     *
     * @param phone User's phone number
     * @param token The OTP token received
     * @return Result containing the authenticated user or an error
     */
    public suspend fun verifyPhoneOtp(
        phone: String,
        token: String
    ): SupabaseResult<AuthUser> {
        _authState.value = AuthState.Loading
        return try {
            client.auth.verifyPhoneOtp(
                type = io.github.jan.supabase.auth.OtpType.Phone.SMS,
                phone = phone,
                token = token
            )
            refreshAuthState()
            val user = currentUser
            if (user != null) {
                SupabaseResult.Success(user)
            } else {
                _authState.value = AuthState.Error("OTP verification succeeded but no user returned")
                SupabaseResult.Failure(
                    SupabaseError.Authentication(message = "OTP verification succeeded but no user returned")
                )
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "OTP verification failed", e)
            SupabaseResult.Failure(
                SupabaseError.Authentication(
                    message = e.message ?: "OTP verification failed",
                    cause = e
                )
            )
        }
    }

    // ========== Password Management ==========

    /**
     * Sends a password reset email.
     *
     * @param email User's email address
     * @param redirectUrl URL to redirect to after password reset
     * @return Result indicating success or an error
     */
    public suspend fun sendPasswordResetEmail(
        email: String,
        redirectUrl: String? = null
    ): SupabaseResult<Unit> {
        return try {
            client.auth.resetPasswordForEmail(
                email = email,
                redirectUrl = redirectUrl
            )
            SupabaseResult.Success(Unit)
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Authentication(
                    message = e.message ?: "Failed to send password reset email",
                    cause = e
                )
            )
        }
    }

    /**
     * Updates the current user's password.
     * User must be authenticated.
     *
     * @param newPassword The new password
     * @return Result indicating success or an error
     */
    public suspend fun updatePassword(newPassword: String): SupabaseResult<Unit> {
        return try {
            client.auth.updateUser {
                password = newPassword
            }
            SupabaseResult.Success(Unit)
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Authentication(
                    message = e.message ?: "Failed to update password",
                    cause = e
                )
            )
        }
    }

    // ========== User Management ==========

    /**
     * Updates the current user's metadata.
     *
     * @param displayName New display name
     * @param avatarUrl New avatar URL
     * @param data Additional metadata
     * @return Result containing the updated user or an error
     */
    public suspend fun updateUser(
        displayName: String? = null,
        avatarUrl: String? = null,
        data: Map<String, Any>? = null
    ): SupabaseResult<AuthUser> {
        return try {
            val metadata = mutableMapOf<String, Any>()
            displayName?.let { metadata["display_name"] = it }
            avatarUrl?.let { metadata["avatar_url"] = it }
            data?.let { metadata.putAll(it) }

            if (metadata.isNotEmpty()) {
                client.auth.updateUser {
                    this.data = buildJsonObject {
                        metadata.forEach { (key, value) ->
                            put(key, value.toString())
                        }
                    }
                }
            }

            refreshAuthState()
            val user = currentUser
            if (user != null) {
                SupabaseResult.Success(user)
            } else {
                SupabaseResult.Failure(
                    SupabaseError.Authentication(message = "Update succeeded but user not found")
                )
            }
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Authentication(
                    message = e.message ?: "Failed to update user",
                    cause = e
                )
            )
        }
    }

    /**
     * Updates the current user's email.
     * A confirmation email will be sent to the new address.
     *
     * @param newEmail New email address
     * @return Result indicating success or an error
     */
    public suspend fun updateEmail(newEmail: String): SupabaseResult<Unit> {
        return try {
            client.auth.updateUser {
                email = newEmail
            }
            SupabaseResult.Success(Unit)
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Authentication(
                    message = e.message ?: "Failed to update email",
                    cause = e
                )
            )
        }
    }

    // ========== Session Management ==========

    /**
     * Signs out the current user.
     *
     * @return Result indicating success or an error
     */
    public suspend fun signOut(): SupabaseResult<Unit> {
        return try {
            client.auth.signOut()
            _authState.value = AuthState.Unauthenticated
            SupabaseResult.Success(Unit)
        } catch (e: Exception) {
            SupabaseResult.Failure(
                SupabaseError.Authentication(
                    message = e.message ?: "Sign out failed",
                    cause = e
                )
            )
        }
    }

    /**
     * Refreshes the current session.
     *
     * @return Result containing the refreshed user or an error
     */
    public suspend fun refreshSession(): SupabaseResult<AuthUser> {
        _authState.value = AuthState.Loading
        return try {
            client.auth.refreshCurrentSession()
            refreshAuthState()
            val user = currentUser
            if (user != null) {
                SupabaseResult.Success(user)
            } else {
                _authState.value = AuthState.Unauthenticated
                SupabaseResult.Failure(
                    SupabaseError.Authentication(message = "Session refresh failed - no user")
                )
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Session refresh failed", e)
            SupabaseResult.Failure(
                SupabaseError.Authentication(
                    message = e.message ?: "Session refresh failed",
                    cause = e
                )
            )
        }
    }

    /**
     * Gets the current access token if available.
     *
     * @return The access token or null if not authenticated
     */
    public fun getAccessToken(): String? {
        return try {
            client.auth.currentAccessTokenOrNull()
        } catch (e: Exception) {
            null
        }
    }

    public companion object {
        /**
         * Returns a SupabaseAuth instance using the default SupabaseCore client.
         *
         * @throws com.dallaslabs.supabase.core.error.SupabaseException.NotConfigured
         *         if SupabaseCore is not initialized
         */
        public fun getInstance(): SupabaseAuth {
            return SupabaseAuth(SupabaseCore.client)
        }

        /**
         * Returns a SupabaseAuth instance if SupabaseCore is initialized, null otherwise.
         */
        public fun getInstanceOrNull(): SupabaseAuth? {
            return SupabaseCore.clientOrNull?.let { SupabaseAuth(it) }
        }
    }
}

/**
 * Extension property to get a SupabaseAuth from a SupabaseCoreClient.
 */
public val SupabaseCoreClient.authentication: SupabaseAuth
    get() = SupabaseAuth(this)
