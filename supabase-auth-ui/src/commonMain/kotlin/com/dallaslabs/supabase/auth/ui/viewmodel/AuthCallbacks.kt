package com.dallaslabs.supabase.auth.ui.viewmodel

/**
 * Callbacks interface for AuthViewModel to communicate with the app
 */
public interface AuthCallbacks {
    /**
     * Called when user successfully authenticates (sign in or account creation with email confirmation)
     * @param userId The authenticated user's ID
     */
    public suspend fun onAuthSuccess(userId: String)

    /**
     * Called when user chooses to continue without an account
     */
    public suspend fun onContinueWithoutAccount()

    /**
     * Optional: Called when user clicks sign in button (for analytics)
     */
    public suspend fun onSignInClick() {}

    /**
     * Optional: Called when user successfully signs in (for analytics)
     * @param method Authentication method (e.g., "email")
     */
    public suspend fun onSignInSuccess(method: String, userId: String) {}

    /**
     * Optional: Called when user successfully creates an account (for analytics)
     * @param method Authentication method (e.g., "email_signup")
     */
    public suspend fun onSignUpSuccess(method: String, userId: String) {}
}
