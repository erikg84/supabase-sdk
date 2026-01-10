package com.dallaslabs.supabase.auth.ui.screen

/**
 * DEPRECATED: This class is no longer used.
 *
 * The AuthScreen now uses Compose Multiplatform string resources with automatic localization
 * for English and Spanish based on the system locale.
 *
 * String resources are located in:
 * - src/commonMain/composeResources/values/strings.xml (English)
 * - src/commonMain/composeResources/values-es/strings.xml (Spanish)
 *
 * If you need to customize strings, you can provide your own translations by
 * adding the appropriate language-specific resource files to your app's resources.
 */
@Deprecated(
    message = "AuthStrings is deprecated. AuthScreen now uses compose resources with automatic localization.",
    level = DeprecationLevel.WARNING
)
public data class AuthStrings(
    // Sign In screen
    val welcomeTitle: String = "Welcome Back",
    val welcomeSubtitle: String = "Sign in to access your account",
    val emailLabel: String = "Email",
    val passwordLabel: String = "Password",
    val forgotPassword: String = "Forgot Password?",
    val signInButton: String = "Sign In",
    val createAccountButton: String = "Create Account",
    val continueWithoutAccount: String = "Continue Without Account",
    val secureMessage: String = "Your data is encrypted and stored securely",

    // Biometric authentication
    val biometricLoginButton: String = "Login with Biometric",
    val loginAs: String = "Logging in as",
    val orDivider: String = "or",

    // Create Account screen
    val createTitle: String = "Create Account",
    val createSubtitle: String = "Join us today",
    val confirmPasswordLabel: String = "Confirm Password",
    val createButton: String = "Create Account",
    val alreadyHaveAccount: String = "Already have an account? Sign in",

    // Forgot Password screen
    val forgotTitle: String = "Reset Password",
    val forgotSubtitle: String = "Enter your email to receive a password reset link",
    val sendResetButton: String = "Send Reset Link",
    val backToSignIn: String = "Back to Sign In",
    val resetInstructions: String = "Check your email for the password reset link"
)
