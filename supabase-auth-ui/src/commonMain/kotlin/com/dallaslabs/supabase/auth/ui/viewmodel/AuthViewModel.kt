package com.dallaslabs.supabase.auth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dallaslabs.supabase.auth.ui.model.AuthResult
import com.dallaslabs.supabase.auth.ui.model.AuthService
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

/**
 * ViewModel for authentication screen.
 * Handles sign in, create account, and password reset with Supabase.
 *
 * This is a generic, reusable ViewModel that uses callbacks for navigation and analytics.
 */
@KoinViewModel
public class AuthViewModel(
    private val authService: AuthService,
    private val callbacks: AuthCallbacks
) : ViewModel() {

    private val _viewState = MutableStateFlow(AuthViewState())
    public val viewState: StateFlow<AuthViewState> = _viewState.asStateFlow()

    private var currentPassword: String = ""

    init {
        // Load saved username if available
        val savedUsername = callbacks.getSavedUsername()
        if (savedUsername != null) {
            updateState { it.copy(email = savedUsername, rememberMe = true) }
        }
    }

    /**
     * Updates the biometric configuration
     * Call this from your app when biometric settings change
     */
    public fun updateBiometricConfig(config: BiometricConfig) {
        updateState { it.copy(biometricConfig = config) }
    }

    public fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.EmailChanged -> updateState { it.copy(email = action.email, emailError = null) }
            is AuthAction.PasswordChanged -> updateState { it.copy(password = action.password, passwordError = null) }
            is AuthAction.ConfirmPasswordChanged -> updateState { it.copy(confirmPassword = action.password, confirmPasswordError = null) }
            is AuthAction.RememberMeChanged -> handleRememberMeChanged(action.remember)
            is AuthAction.SignInClicked -> signIn()
            is AuthAction.CreateAccountClicked -> createAccount()
            is AuthAction.ForgotPasswordClicked -> {}
            is AuthAction.SendResetLinkClicked -> sendPasswordReset()
            is AuthAction.SwitchToSignIn -> onSwitchToSignIn()
            is AuthAction.SwitchToCreateAccount -> onSwitchToAccountCreate()
            is AuthAction.SwitchToForgotPassword -> onSwitchToForgotPassword()
            is AuthAction.ContinueWithoutAccount -> onContinueWithoutAccount()
            is AuthAction.DismissError -> updateState { it.copy(errorMessage = null, successMessage = null) }
            is AuthAction.BiometricAuthClicked -> handleBiometricAuth()
        }
    }

    private fun handleRememberMeChanged(remember: Boolean) {
        updateState { it.copy(rememberMe = remember) }
    }

    private fun onSwitchToForgotPassword() {
        updateState {
            it.copy(
                mode = AuthMode.FORGOT_PASSWORD,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private fun onSwitchToAccountCreate() {
        updateState {
            it.copy(
                mode = AuthMode.CREATE_ACCOUNT,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private fun onSwitchToSignIn() {
        updateState {
            it.copy(
                mode = AuthMode.SIGN_IN,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private fun onContinueWithoutAccount() {
        viewModelScope.launch {
            callbacks.onContinueWithoutAccount()
        }
    }

    private fun signIn() {
        val state = _viewState.value

        if (!validateEmail(state.email)) return
        if (!validatePassword(state.password)) return

        currentPassword = state.password

        viewModelScope.launch {
            callbacks.onSignInClick()
            setLoadingState(true)
            when (val result = authService.signInWithEmailAndPassword(state.email, state.password)) {
                is AuthResult.Success -> {
                    Napier.i("AuthViewModel: Sign in successful")
                    result.user?.uid?.let { userId ->
                        // Save credentials for biometric login
                        callbacks.onCredentialsShouldBeSaved(state.email, currentPassword)

                        // Save remember me preference
                        callbacks.onRememberMeChanged(state.email, state.rememberMe)

                        callbacks.onSignInSuccess("email", userId)
                        callbacks.onAuthSuccess(userId, state.email)
                    }
                }
                is AuthResult.Error -> {
                    Napier.e("AuthViewModel: Sign in failed: ${result.message}")
                    updateState { it.copy(errorMessage = result.message) }
                }
            }
            setLoadingState(false)
            currentPassword = ""
        }
    }

    private fun createAccount() {
        val state = _viewState.value

        if (!validateEmail(state.email)) return
        if (!validatePassword(state.password)) return
        if (!validateConfirmPassword(state.password, state.confirmPassword)) return

        currentPassword = state.password

        viewModelScope.launch {
            setLoadingState(true)
            when (val result = authService.createUserWithEmailAndPassword(state.email, state.password)) {
                is AuthResult.Success -> {
                    if (result.user != null) {
                        // User is already verified (email confirmation disabled)
                        Napier.i("AuthViewModel: Account created and verified")

                        // Save credentials for biometric login
                        callbacks.onCredentialsShouldBeSaved(state.email, currentPassword)

                        // Save remember me preference
                        callbacks.onRememberMeChanged(state.email, state.rememberMe)

                        callbacks.onSignUpSuccess("email_signup", result.user.uid)
                        callbacks.onAuthSuccess(result.user.uid, state.email)
                    } else {
                        // Email confirmation required
                        Napier.i("AuthViewModel: Account created, email confirmation pending")
                        updateState {
                            it.copy(
                                successMessage = "Account created! Please check your email to confirm your account.",
                                mode = AuthMode.SIGN_IN,
                                password = "",
                                confirmPassword = ""
                            )
                        }
                    }
                }
                is AuthResult.Error -> {
                    Napier.e("AuthViewModel: Account creation failed: ${result.message}")
                    updateState { it.copy(errorMessage = result.message) }
                }
            }
            setLoadingState(false)
            currentPassword = ""
        }
    }

    private fun handleBiometricAuth() {
        val state = _viewState.value
        val savedUsername = state.biometricConfig.savedUsername

        if (savedUsername == null) {
            Napier.e("AuthViewModel: No saved username for biometric auth")
            updateState { it.copy(errorMessage = "Biometric login not configured") }
            return
        }

        viewModelScope.launch {
            setLoadingState(true)

            when (val biometricResult = callbacks.onBiometricAuthRequested(savedUsername)) {
                is BiometricAuthResult.Success -> {
                    Napier.i("AuthViewModel: Biometric auth successful, signing in")
                    when (val authResult = authService.signInWithEmailAndPassword(savedUsername, biometricResult.password)) {
                        is AuthResult.Success -> {
                            authResult.user?.uid?.let { userId ->
                                callbacks.onSignInSuccess("biometric", userId)
                                callbacks.onAuthSuccess(userId, savedUsername)
                            }
                        }
                        is AuthResult.Error -> {
                            Napier.e("AuthViewModel: Backend auth failed after biometric: ${authResult.message}")
                            updateState { it.copy(errorMessage = authResult.message) }
                        }
                    }
                }
                is BiometricAuthResult.Failed -> {
                    Napier.e("AuthViewModel: Biometric auth failed: ${biometricResult.reason}")
                    updateState { it.copy(errorMessage = biometricResult.reason ?: "Biometric authentication failed") }
                }
                is BiometricAuthResult.Cancelled -> {
                    Napier.i("AuthViewModel: Biometric auth cancelled by user")
                    // No error message for user cancellation
                }
            }

            setLoadingState(false)
        }
    }

    private fun sendPasswordReset() {
        val state = _viewState.value

        if (!validateEmail(state.email)) return

        viewModelScope.launch {
            setLoadingState(true)
            when (val result = authService.sendPasswordResetEmail(state.email)) {
                is AuthResult.Success -> {
                    Napier.i("AuthViewModel: Password reset email sent")
                    updateState {
                        it.copy(
                            successMessage = "Password reset email sent to ${state.email}",
                            mode = AuthMode.SIGN_IN
                        )
                    }
                }
                is AuthResult.Error -> {
                    Napier.e("AuthViewModel: Password reset failed: ${result.message}")
                    updateState { it.copy(errorMessage = result.message) }
                }
            }
            setLoadingState(false)
        }
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isBlank()) {
            updateState { it.copy(emailError = ValidationError.EmailEmpty) }
            return false
        }
        if (!email.contains("@") || !email.contains(".")) {
            updateState { it.copy(emailError = ValidationError.EmailInvalid) }
            return false
        }
        return true
    }

    private fun validatePassword(password: String): Boolean {
        if (password.isBlank()) {
            updateState { it.copy(passwordError = ValidationError.PasswordEmpty) }
            return false
        }
        if (password.length < 6) {
            updateState { it.copy(passwordError = ValidationError.PasswordTooShort) }
            return false
        }
        return true
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        if (password != confirmPassword) {
            updateState { it.copy(confirmPasswordError = ValidationError.PasswordsMismatch) }
            return false
        }
        return true
    }

    private fun setLoadingState(isLoading: Boolean) {
        updateState { it.copy(isLoading = isLoading) }
    }

    private inline fun updateState(reducer: (AuthViewState) -> AuthViewState) {
        _viewState.value = reducer(_viewState.value)
    }
}
