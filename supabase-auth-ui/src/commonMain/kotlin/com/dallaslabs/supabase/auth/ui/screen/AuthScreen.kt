package com.dallaslabs.supabase.auth.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

/**
 * Authentication screen with sign in, create account, and forgot password modes.
 *
 * @param viewState Current state of the auth screen
 * @param onAction Callback for user actions
 * @param strings Customizable strings for the screen (defaults to English)
 * @param modifier Modifier for the root composable
 */
@Composable
public fun AuthScreen(
    viewState: AuthViewState,
    onAction: (AuthAction) -> Unit,
    strings: AuthStrings = AuthStrings(),
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (viewState.mode) {
            AuthMode.SIGN_IN -> SignInContent(viewState, onAction, strings)
            AuthMode.CREATE_ACCOUNT -> CreateAccountContent(viewState, onAction, strings)
            AuthMode.FORGOT_PASSWORD -> ForgotPasswordContent(viewState, onAction, strings)
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun SignInContent(
    viewState: AuthViewState,
    onAction: (AuthAction) -> Unit,
    strings: AuthStrings
) {
    Text(
        text = strings.welcomeTitle,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = strings.welcomeSubtitle,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(32.dp))

    EmailField(
        value = viewState.email,
        onValueChange = { onAction(AuthAction.EmailChanged(it)) },
        label = strings.emailLabel,
        error = viewState.emailError,
        enabled = !viewState.isLoading
    )

    Spacer(modifier = Modifier.height(16.dp))

    PasswordField(
        value = viewState.password,
        onValueChange = { onAction(AuthAction.PasswordChanged(it)) },
        label = strings.passwordLabel,
        error = viewState.passwordError,
        enabled = !viewState.isLoading,
        imeAction = ImeAction.Done
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(
            onClick = { onAction(AuthAction.SwitchToForgotPassword) }
        ) {
            Text(strings.forgotPassword)
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = { onAction(AuthAction.SignInClicked) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !viewState.isLoading
    ) {
        if (viewState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(strings.signInButton)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = { onAction(AuthAction.SwitchToCreateAccount) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !viewState.isLoading
    ) {
        Text(strings.createAccountButton)
    }

    Spacer(modifier = Modifier.height(24.dp))

    HorizontalDivider()

    Spacer(modifier = Modifier.height(24.dp))

    TextButton(
        onClick = { onAction(AuthAction.ContinueWithoutAccount) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(strings.continueWithoutAccount)
    }
}

@Composable
private fun CreateAccountContent(
    viewState: AuthViewState,
    onAction: (AuthAction) -> Unit,
    strings: AuthStrings
) {
    Text(
        text = strings.createTitle,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = strings.createSubtitle,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(32.dp))

    EmailField(
        value = viewState.email,
        onValueChange = { onAction(AuthAction.EmailChanged(it)) },
        label = strings.emailLabel,
        error = viewState.emailError,
        enabled = !viewState.isLoading
    )

    Spacer(modifier = Modifier.height(16.dp))

    PasswordField(
        value = viewState.password,
        onValueChange = { onAction(AuthAction.PasswordChanged(it)) },
        label = strings.passwordLabel,
        error = viewState.passwordError,
        enabled = !viewState.isLoading,
        imeAction = ImeAction.Next
    )

    Spacer(modifier = Modifier.height(16.dp))

    PasswordField(
        value = viewState.confirmPassword,
        onValueChange = { onAction(AuthAction.ConfirmPasswordChanged(it)) },
        label = strings.confirmPasswordLabel,
        error = viewState.confirmPasswordError,
        enabled = !viewState.isLoading,
        imeAction = ImeAction.Done
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = { onAction(AuthAction.CreateAccountClicked) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !viewState.isLoading
    ) {
        if (viewState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(strings.createButton)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(
        onClick = { onAction(AuthAction.SwitchToSignIn) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(strings.alreadyHaveAccount)
    }
}

@Composable
private fun ForgotPasswordContent(
    viewState: AuthViewState,
    onAction: (AuthAction) -> Unit,
    strings: AuthStrings
) {
    Text(
        text = strings.forgotTitle,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = strings.forgotSubtitle,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(32.dp))

    EmailField(
        value = viewState.email,
        onValueChange = { onAction(AuthAction.EmailChanged(it)) },
        label = strings.emailLabel,
        error = viewState.emailError,
        enabled = !viewState.isLoading
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = { onAction(AuthAction.SendResetLinkClicked) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !viewState.isLoading
    ) {
        if (viewState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(strings.sendResetButton)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(
        onClick = { onAction(AuthAction.SwitchToSignIn) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(strings.backToSignIn)
    }
}

@Composable
private fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
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
    error: String?,
    enabled: Boolean,
    imeAction: ImeAction,
    modifier: Modifier = Modifier
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
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
