package com.dallaslabs.supabase.auth.ui.viewmodel

/**
 * State for the authentication screen
 */
public data class AuthViewState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
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
    public data object SignInClicked : AuthAction
    public data object CreateAccountClicked : AuthAction
    public data object ForgotPasswordClicked : AuthAction
    public data object SendResetLinkClicked : AuthAction
    public data object SwitchToSignIn : AuthAction
    public data object SwitchToCreateAccount : AuthAction
    public data object SwitchToForgotPassword : AuthAction
    public data object ContinueWithoutAccount : AuthAction
    public data object DismissError : AuthAction
}
