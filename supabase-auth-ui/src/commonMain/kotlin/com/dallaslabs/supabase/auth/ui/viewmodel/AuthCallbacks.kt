package com.dallaslabs.supabase.auth.ui.viewmodel

/**
 * Callbacks interface for AuthViewModel to communicate with the app
 */
public interface AuthCallbacks {
    /**
     * Called when user successfully authenticates (sign in or account creation with email confirmation)
     * @param userId The authenticated user's ID
     * @param username The user's email/username (for saving in secure storage)
     */
    public suspend fun onAuthSuccess(userId: String, username: String)

    /**
     * Called when user chooses to continue without an account
     */
    public suspend fun onContinueWithoutAccount()

    /**
     * Called when user requests biometric authentication
     * The app should trigger biometric prompt and call back with result
     * @param username The saved username to authenticate with
     * @return BiometricAuthResult indicating success or failure
     */
    public suspend fun onBiometricAuthRequested(username: String): BiometricAuthResult

    /**
     * Called when credentials should be saved for biometric login
     * The app should securely store these credentials (e.g., in encrypted preferences or keystore)
     * @param username The user's email/username
     * @param password The user's password (for later biometric authentication)
     */
    public suspend fun onCredentialsShouldBeSaved(username: String, password: String) {}

    /**
     * Called when "Remember me" checkbox state changes
     * The app should persist this preference
     * @param username The user's email
     * @param remember Whether to remember the username
     */
    public suspend fun onRememberMeChanged(username: String, remember: Boolean) {}

    /**
     * Get the saved username if "Remember me" was enabled
     * @return The saved username or null if not saved
     */
    public fun getSavedUsername(): String? = null

    /**
     * Optional: Called when user clicks sign in button (for analytics)
     */
    public suspend fun onSignInClick() {}

    /**
     * Optional: Called when user successfully signs in (for analytics)
     * @param method Authentication method (e.g., "email", "biometric")
     * @param userId The authenticated user's ID
     */
    public suspend fun onSignInSuccess(method: String, userId: String) {}

    /**
     * Optional: Called when user successfully creates an account (for analytics)
     * @param method Authentication method (e.g., "email_signup")
     * @param userId The authenticated user's ID
     */
    public suspend fun onSignUpSuccess(method: String, userId: String) {}
}

/**
 * Result of biometric authentication from the app
 */
public sealed interface BiometricAuthResult {
    /**
     * User authenticated successfully via biometrics
     * @param password The user's stored password for backend authentication
     */
    public data class Success(val password: String) : BiometricAuthResult

    /**
     * Authentication failed (wrong biometric, cancelled, or error)
     * @param reason Optional reason for failure
     */
    public data class Failed(val reason: String? = null) : BiometricAuthResult

    /**
     * User cancelled the biometric prompt
     */
    public data object Cancelled : BiometricAuthResult
}
