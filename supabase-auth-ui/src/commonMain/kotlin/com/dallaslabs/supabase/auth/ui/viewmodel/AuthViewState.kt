package com.dallaslabs.supabase.auth.ui.viewmodel

/**
 * Validation error types for localized error messages
 */
public sealed interface ValidationError {
    public data object EmailEmpty : ValidationError
    public data object EmailInvalid : ValidationError
    public data object PasswordEmpty : ValidationError
    public data object PasswordTooShort : ValidationError
    public data object PasswordsMismatch : ValidationError
}

/**
 * State for the authentication screen
 */
public data class AuthViewState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val confirmPasswordError: ValidationError? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val biometricConfig: BiometricConfig = BiometricConfig(),
    val rememberMe: Boolean = false
)

/**
 * Configuration for biometric authentication
 */
public data class BiometricConfig(
    val enabled: Boolean = false,
    val available: Boolean = false,
    val savedUsername: String? = null
)

/**
 * Auth screen modes
 */
public enum class AuthMode {
    SIGN_IN,
    CREATE_ACCOUNT,
    FORGOT_PASSWORD
}

/**
 * Actions for authentication screen
 */
public sealed interface AuthAction {
    public data class EmailChanged(val email: String) : AuthAction
    public data class PasswordChanged(val password: String) : AuthAction
    public data class ConfirmPasswordChanged(val password: String) : AuthAction
    public data class RememberMeChanged(val remember: Boolean) : AuthAction
    public data object SignInClicked : AuthAction
    public data object CreateAccountClicked : AuthAction
    public data object ForgotPasswordClicked : AuthAction
    public data object SendResetLinkClicked : AuthAction
    public data object SwitchToSignIn : AuthAction
    public data object SwitchToCreateAccount : AuthAction
    public data object SwitchToForgotPassword : AuthAction
    public data object ContinueWithoutAccount : AuthAction
    public data object DismissError : AuthAction
    public data object BiometricAuthClicked : AuthAction
}
