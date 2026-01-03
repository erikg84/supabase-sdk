package com.dallaslabs.supabase.auth

import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Represents an authenticated user.
 *
 * This is a simplified view of the Supabase user with commonly needed properties.
 */
@Serializable
public data class AuthUser(
    /** Unique user identifier (UUID) */
    val id: String,
    /** User's email address */
    val email: String?,
    /** User's phone number */
    val phone: String?,
    /** Display name from user metadata */
    val displayName: String?,
    /** Avatar URL from user metadata */
    val avatarUrl: String?,
    /** Whether the email has been confirmed */
    val emailConfirmed: Boolean,
    /** Whether the phone has been confirmed */
    val phoneConfirmed: Boolean,
    /** When the user was created (ISO 8601) */
    val createdAt: String?,
    /** When the user last signed in (ISO 8601) */
    val lastSignInAt: String?,
    /** The authentication provider used */
    val provider: String?,
    /** Raw user metadata as JSON */
    val metadata: Map<String, String>
) {
    public companion object {
        /**
         * Creates an AuthUser from a Supabase UserInfo.
         */
        @PublishedApi
        internal fun fromUserInfo(userInfo: UserInfo): AuthUser {
            val metadata = mutableMapOf<String, String>()
            userInfo.userMetadata?.forEach { (key, value) ->
                metadata[key] = value.toString()
            }

            return AuthUser(
                id = userInfo.id,
                email = userInfo.email,
                phone = userInfo.phone,
                displayName = metadata["display_name"] ?: metadata["full_name"] ?: metadata["name"],
                avatarUrl = metadata["avatar_url"] ?: metadata["picture"],
                emailConfirmed = userInfo.emailConfirmedAt != null,
                phoneConfirmed = userInfo.phoneConfirmedAt != null,
                createdAt = userInfo.createdAt?.toString(),
                lastSignInAt = userInfo.lastSignInAt?.toString(),
                provider = userInfo.appMetadata?.get("provider")?.toString(),
                metadata = metadata
            )
        }
    }
}

/**
 * Represents the current authentication state.
 */
public sealed class AuthState {
    /** Initial state, authentication status unknown */
    public data object Unknown : AuthState()

    /** Currently checking authentication status */
    public data object Loading : AuthState()

    /** User is authenticated */
    public data class Authenticated(val user: AuthUser) : AuthState()

    /** User is not authenticated */
    public data object Unauthenticated : AuthState()

    /** Authentication error occurred */
    public data class Error(val message: String, val cause: Throwable? = null) : AuthState()

    /** Whether the user is currently authenticated */
    public val isAuthenticated: Boolean
        get() = this is Authenticated

    /** Get the current user if authenticated, null otherwise */
    public val currentUser: AuthUser?
        get() = (this as? Authenticated)?.user
}
