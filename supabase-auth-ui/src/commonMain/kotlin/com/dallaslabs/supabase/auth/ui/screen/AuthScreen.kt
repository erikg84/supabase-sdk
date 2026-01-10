package com.dallaslabs.supabase.auth.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dallaslabs.supabase.auth.ui.viewmodel.AuthAction
import com.dallaslabs.supabase.auth.ui.viewmodel.AuthMode
import com.dallaslabs.supabase.auth.ui.viewmodel.AuthViewState
import com.dallaslabs.supabase.auth.ui.viewmodel.ValidationError
import org.jetbrains.compose.resources.stringResource
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.Res
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_welcome_title
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_welcome_subtitle
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_email_label
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_password_label
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_forgot_password
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_sign_in_button
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_create_account_button
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_continue_without_account
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_secure_message
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_remember_me
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_biometric_login_button
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_login_as
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_or_divider
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_create_title
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_create_subtitle
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_confirm_password_label
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_create_button
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_already_have_account
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_forgot_title
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_forgot_subtitle
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_send_reset_button
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_back_to_sign_in
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_reset_instructions
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_error_email_empty
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_error_email_invalid
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_error_password_empty
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_error_password_short
import com.dallaslabs.sdk.supabase_auth_ui.generated.resources.auth_error_passwords_mismatch

/**
 * Authentication screen with sign in, create account, and forgot password modes.
 *
 * Supports automatic localization for English and Spanish.
 * The screen will use the system locale to display strings in the appropriate language.
 *
 * @param viewState Current state of the auth screen
 * @param onAction Callback for user actions
 * @param modifier Modifier for the root composable
 */
@Composable
public fun AuthScreen(
    viewState: AuthViewState,
    onAction: (AuthAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewState.errorMessage) {
        viewState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onAction(AuthAction.DismissError)
        }
    }

    LaunchedEffect(viewState.successMessage) {
        viewState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onAction(AuthAction.DismissError)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Icon
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Security",
                modifier = Modifier
                    .size(72.dp)
                    .semantics { contentDescription = "Secure authentication" },
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (viewState.mode) {
                AuthMode.SIGN_IN -> SignInContent(viewState, onAction)
                AuthMode.CREATE_ACCOUNT -> CreateAccountContent(viewState, onAction)
                AuthMode.FORGOT_PASSWORD -> ForgotPasswordContent(viewState, onAction)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun SignInContent(
    viewState: AuthViewState,
    onAction: (AuthAction) -> Unit
) {
    Text(
        text = stringResource(Res.string.auth_welcome_title),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(Res.string.auth_welcome_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(32.dp))

    // Main auth card
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Biometric login option (if enabled)
            AnimatedVisibility(
                visible = viewState.biometricConfig.enabled &&
                        viewState.biometricConfig.available &&
                        viewState.biometricConfig.savedUsername != null
            ) {
                Column {
                    FilledTonalButton(
                        onClick = { onAction(AuthAction.BiometricAuthClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .semantics { contentDescription = "Login with biometrics" },
                        enabled = !viewState.isLoading
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Fingerprint,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = stringResource(Res.string.auth_biometric_login_button),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    viewState.biometricConfig.savedUsername?.let { username ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${stringResource(Res.string.auth_login_as)} $username",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text = stringResource(Res.string.auth_or_divider),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            EmailField(
                value = viewState.email,
                onValueChange = { onAction(AuthAction.EmailChanged(it)) },
                label = stringResource(Res.string.auth_email_label),
                error = viewState.emailError,
                enabled = !viewState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                value = viewState.password,
                onValueChange = { onAction(AuthAction.PasswordChanged(it)) },
                label = stringResource(Res.string.auth_password_label),
                error = viewState.passwordError,
                enabled = !viewState.isLoading,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = viewState.rememberMe,
                        onCheckedChange = { onAction(AuthAction.RememberMeChanged(it)) },
                        enabled = !viewState.isLoading
                    )
                    Text(
                        text = stringResource(Res.string.auth_remember_me),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(
                    onClick = { onAction(AuthAction.SwitchToForgotPassword) },
                    enabled = !viewState.isLoading
                ) {
                    Text(stringResource(Res.string.auth_forgot_password))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onAction(AuthAction.SignInClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !viewState.isLoading
            ) {
                if (viewState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.auth_sign_in_button),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onAction(AuthAction.SwitchToCreateAccount) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !viewState.isLoading
            ) {
                Text(
                    text = stringResource(Res.string.auth_create_account_button),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    TextButton(
        onClick = { onAction(AuthAction.ContinueWithoutAccount) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !viewState.isLoading
    ) {
        Text(stringResource(Res.string.auth_continue_without_account))
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(Res.string.auth_secure_message),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun CreateAccountContent(
    viewState: AuthViewState,
    onAction: (AuthAction) -> Unit
) {
    Text(
        text = stringResource(Res.string.auth_create_title),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(Res.string.auth_create_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(32.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            EmailField(
                value = viewState.email,
                onValueChange = { onAction(AuthAction.EmailChanged(it)) },
                label = stringResource(Res.string.auth_email_label),
                error = viewState.emailError,
                enabled = !viewState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                value = viewState.password,
                onValueChange = { onAction(AuthAction.PasswordChanged(it)) },
                label = stringResource(Res.string.auth_password_label),
                error = viewState.passwordError,
                enabled = !viewState.isLoading,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                value = viewState.confirmPassword,
                onValueChange = { onAction(AuthAction.ConfirmPasswordChanged(it)) },
                label = stringResource(Res.string.auth_confirm_password_label),
                error = viewState.confirmPasswordError,
                enabled = !viewState.isLoading,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onAction(AuthAction.CreateAccountClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !viewState.isLoading
            ) {
                if (viewState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.auth_create_button),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { onAction(AuthAction.SwitchToSignIn) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewState.isLoading
            ) {
                Text(stringResource(Res.string.auth_already_have_account))
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(Res.string.auth_secure_message),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ForgotPasswordContent(
    viewState: AuthViewState,
    onAction: (AuthAction) -> Unit
) {
    Text(
        text = stringResource(Res.string.auth_forgot_title),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(Res.string.auth_forgot_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(32.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            EmailField(
                value = viewState.email,
                onValueChange = { onAction(AuthAction.EmailChanged(it)) },
                label = stringResource(Res.string.auth_email_label),
                error = viewState.emailError,
                enabled = !viewState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onAction(AuthAction.SendResetLinkClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !viewState.isLoading
            ) {
                if (viewState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.auth_send_reset_button),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { onAction(AuthAction.SwitchToSignIn) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewState.isLoading
            ) {
                Text(stringResource(Res.string.auth_back_to_sign_in))
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(Res.string.auth_reset_instructions),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun resolveValidationError(error: ValidationError?): String? {
    return error?.let {
        when (it) {
            ValidationError.EmailEmpty -> stringResource(Res.string.auth_error_email_empty)
            ValidationError.EmailInvalid -> stringResource(Res.string.auth_error_email_invalid)
            ValidationError.PasswordEmpty -> stringResource(Res.string.auth_error_password_empty)
            ValidationError.PasswordTooShort -> stringResource(Res.string.auth_error_password_short)
            ValidationError.PasswordsMismatch -> stringResource(Res.string.auth_error_passwords_mismatch)
        }
    }
}

@Composable
private fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: ValidationError?,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val errorText = resolveValidationError(error)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        isError = errorText != null,
        supportingText = errorText?.let { { Text(it) } },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: ValidationError?,
    enabled: Boolean,
    imeAction: ImeAction,
    modifier: Modifier = Modifier
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val errorText = resolveValidationError(error)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        isError = errorText != null,
        supportingText = errorText?.let { { Text(it) } },
        enabled = enabled,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                )
            }
        }
    )
}
