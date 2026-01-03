package com.dallaslabs.supabase.auth.ui.screen

/**
 * Customizable strings for AuthScreen.
 * Apps can provide their own translations by creating an instance with custom values.
 */
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
    val backToSignIn: String = "Back to Sign In"
)
