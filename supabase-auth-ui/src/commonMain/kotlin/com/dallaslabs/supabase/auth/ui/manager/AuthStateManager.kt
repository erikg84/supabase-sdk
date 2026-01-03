package com.dallaslabs.supabase.auth.ui.manager

import com.dallaslabs.supabase.auth.ui.model.AuthService
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

/**
 * Manages auth state changes and handles callbacks based on authentication status.
 *
 * @param authService The authentication service to observe
 * @param callbacks Callbacks for auth state changes
 */
@Single
public class AuthStateManager(
    private val authService: AuthService,
    private val callbacks: AuthStateCallbacks
) {
    private val scope: CoroutineScope = MainScope()
    private var previousAuthState: Boolean? = null

    /**
     * Start listening to auth state changes
     */
    public fun startListening() {
        authService.currentUser
            .onEach { user ->
                val isAuthenticated = user != null
                handleAuthStateChange(isAuthenticated, user?.uid)
            }
            .launchIn(scope)
    }

    /**
     * Handle deep link auth callback.
     * Call this when the app receives a deep link intent/URL.
     */
    public fun handleAuthCallback() {
        Napier.i("AuthStateManager: Handling auth callback")
        Napier.i("AuthStateManager: Current auth state: ${authService.isAuthenticated()}")
        Napier.i("AuthStateManager: Current user: ${authService.currentUser.value}")

        scope.launch {
            // Force check auth state after deep link
            val isAuthenticated = authService.isAuthenticated()
            Napier.i("AuthStateManager: Post-callback auth check: $isAuthenticated")
            if (isAuthenticated) {
                Napier.i("AuthStateManager: User is now authenticated after callback")
            } else {
                Napier.i("AuthStateManager: User is still not authenticated after callback")
            }
        }
    }

    private fun handleAuthStateChange(isAuthenticated: Boolean, userId: String?) {
        // Only react to actual state changes
        if (previousAuthState == isAuthenticated) return
        previousAuthState = isAuthenticated

        Napier.i("AuthStateManager: Auth state changed - authenticated: $isAuthenticated")

        scope.launch {
            if (isAuthenticated && userId != null) {
                // User just logged in
                callbacks.onUserAuthenticated(userId)
                Napier.i("AuthStateManager: User authenticated")
            } else {
                // User just logged out
                callbacks.onUserSignedOut()
                Napier.i("AuthStateManager: User signed out")
            }
        }
    }
}

/**
 * Callbacks for auth state changes
 */
public interface AuthStateCallbacks {
    /**
     * Called when user becomes authenticated
     * @param userId The authenticated user's ID
     */
    public suspend fun onUserAuthenticated(userId: String)

    /**
     * Called when user signs out
     */
    public suspend fun onUserSignedOut()
}
