# SDK Auth UI Architecture Review

**Date:** 2026-01-08
**Reviewed By:** Development Team
**Purpose:** Comprehensive analysis of auth UI SDK for use in multiple applications

---

## Executive Summary

This document analyzes the current state of the `supabase-auth-ui` module, identifies critical architectural issues, and provides recommendations for improvement. The SDK will be used across multiple apps, so it's essential that the responsibility boundaries between SDK and consuming apps are clear and well-designed.

---

## Current Architecture

### Module Structure
```
supabase-auth-ui/
├── src/commonMain/kotlin/com/dallaslabs/supabase/auth/ui/
│   ├── viewmodel/
│   │   ├── AuthViewModel.kt      # Business logic
│   │   ├── AuthCallbacks.kt      # App communication interface
│   │   └── AuthViewState.kt      # State + Actions + BiometricConfig
│   ├── screen/
│   │   ├── AuthScreen.kt         # Composable UI
│   │   └── AuthStrings.kt        # Deprecated - using resources
│   ├── model/
│   │   └── AuthModels.kt         # AuthService, AuthUser, AuthResult
│   └── di/
│       └── SupabaseAuthUIModule.kt
├── src/commonMain/composeResources/
│   ├── values/strings.xml        # English strings
│   └── values-es/strings.xml     # Spanish strings
```

### Component Responsibilities

#### AuthViewModel (SDK)
- Manages form state (email, password, confirmPassword)
- Validates input (email format, password length, password match)
- Calls AuthService for authentication operations
- Triggers callbacks on success/failure
- Handles biometric authentication flow

#### AuthScreen (SDK)
- Renders sign-in, create account, forgot password UI
- Uses localized string resources
- Shows biometric button when enabled
- Displays errors via Snackbar

#### AuthCallbacks (Interface - App Implements)
- `onAuthSuccess(userId, username)` - Navigate after auth
- `onContinueWithoutAccount()` - Guest mode navigation
- `onBiometricAuthRequested(username)` - Return saved password
- Analytics callbacks (optional)

#### AuthService (Interface - App Implements)
- `signInWithEmailAndPassword(email, password)`
- `createUserWithEmailAndPassword(email, password)`
- `sendPasswordResetEmail(email)`
- `updatePassword(newPassword)`
- `deleteAccount()`
- `signOut()`

#### BiometricConfig (App Provides)
- `enabled: Boolean` - Show biometric button?
- `available: Boolean` - Is biometric hardware available?
- `savedUsername: String?` - Username for biometric auth

---

## Data Flow Analysis

### Sign In Flow
```
User enters email/password
  → AuthAction.SignInClicked
  → AuthViewModel.signIn()
  → Validate email/password
  → authService.signInWithEmailAndPassword(email, password)
  → AuthResult.Success
  → callbacks.onSignInSuccess("email", userId)
  → callbacks.onAuthSuccess(userId, email)
  → App navigates to authenticated state
```

### Biometric Auth Flow
```
User taps biometric button
  → AuthAction.BiometricAuthClicked
  → AuthViewModel.handleBiometricAuth()
  → callbacks.onBiometricAuthRequested(savedUsername)
  → App triggers biometric prompt
  → App retrieves stored password
  → BiometricAuthResult.Success(password)
  → authService.signInWithEmailAndPassword(savedUsername, password)
  → callbacks.onAuthSuccess(userId, savedUsername)
```

---

## Critical Issues Identified

### Issue #1: Password Storage Responsibility is Unclear

**Problem:**
The SDK expects the app to return the user's password in `BiometricAuthResult.Success(password)`, but the SDK never provides the password to the app for storage.

**Current Flow:**
1. User signs in with email/password via SDK
2. SDK calls `onAuthSuccess(userId, username)` - NO PASSWORD!
3. Later, user wants biometric login
4. SDK calls `onBiometricAuthRequested(username)`
5. App must return the password... but it was never saved!

**Evidence from BiometricsLogin:**
```kotlin
// App.kt - Had to hardcode password because SDK never provided it
override suspend fun onAuthSuccess(userId: String, username: String) {
    val biometricEnabled = preferencesManager.getBoolean(AuthPreferenceKeys.BIOMETRIC_ENABLED, false)
    if (biometricEnabled) {
        val currentPassword = "password123"  // ← WHERE DOES THIS COME FROM?
        preferencesManager.saveString("saved_password_$username", currentPassword)
    }
}
```

**Impact:**
- Apps cannot implement biometric login correctly
- Security risk if apps hack around this limitation
- Inconsistent implementation across apps

**Recommendation:**
Add password to the callback or provide a secure storage mechanism:

**Option A: Add password to callback**
```kotlin
interface AuthCallbacks {
    suspend fun onAuthSuccess(userId: String, username: String, password: String)
}
```

**Option B: Add credential storage callback**
```kotlin
interface AuthCallbacks {
    suspend fun onAuthSuccess(userId: String, username: String)
    suspend fun onCredentialsShouldBeSaved(username: String, password: String) // NEW
}
```

**Option C: SDK handles secure storage internally**
```kotlin
// SDK stores credentials securely using platform keystore
// BiometricConfig just enables/disables, SDK handles the rest
```

---

### Issue #2: No "Remember Me" / "Save Username" Checkbox

**Problem:**
The SDK UI doesn't include a checkbox for "Remember me" or "Save email for next time". This means:
- Apps must track this preference separately
- No standard way to save/restore username
- BiometricConfig.savedUsername must be set externally

**Current State:**
- SDK just has email text field
- No persistence of email between sessions
- No user choice to "remember" their email

**Evidence from BiometricsLogin:**
```kotlin
// App has to manage saved username completely outside SDK
val savedUsername = preferencesManager.getString(AuthPreferenceKeys.SAVED_USERNAME)
authViewModel.updateBiometricConfig(
    BiometricConfig(
        savedUsername = savedUsername  // App manages this
    )
)
```

**Recommendation:**
Add optional "Remember me" checkbox to SDK:
```kotlin
data class AuthViewState(
    // ... existing fields
    val rememberMe: Boolean = false,
    val initialEmail: String? = null  // Pre-filled from saved
)

sealed interface AuthAction {
    data class RememberMeChanged(val enabled: Boolean) : AuthAction
}

interface AuthCallbacks {
    suspend fun onSaveUsernamePreference(username: String, remember: Boolean)
    fun getSavedUsername(): String?
}
```

---

### Issue #3: Validation Error Messages Not Localized

**Problem:**
While UI strings are properly localized (English + Spanish), validation error messages are hardcoded in English:

**Hardcoded Strings in AuthViewModel.kt:**
```kotlin
// Line 235-241
if (email.isBlank()) {
    updateState { it.copy(emailError = "Email cannot be empty") }  // ← NOT LOCALIZED
}
if (!email.contains("@") || !email.contains(".")) {
    updateState { it.copy(emailError = "Please enter a valid email address") }  // ← NOT LOCALIZED
}

// Line 246-253
if (password.isBlank()) {
    updateState { it.copy(passwordError = "Password cannot be empty") }  // ← NOT LOCALIZED
}
if (password.length < 6) {
    updateState { it.copy(passwordError = "Password must be at least 6 characters") }  // ← NOT LOCALIZED
}

// Line 258-260
if (password != confirmPassword) {
    updateState { it.copy(confirmPasswordError = "Passwords do not match") }  // ← NOT LOCALIZED
}
```

**Impact:**
- Spanish users see English error messages
- Inconsistent UX

**Recommendation:**
Add error strings to resources:

**values/strings.xml:**
```xml
<string name="auth_error_email_empty">Email cannot be empty</string>
<string name="auth_error_email_invalid">Please enter a valid email address</string>
<string name="auth_error_password_empty">Password cannot be empty</string>
<string name="auth_error_password_short">Password must be at least 6 characters</string>
<string name="auth_error_passwords_mismatch">Passwords do not match</string>
<string name="auth_error_biometric_not_configured">Biometric login not configured</string>
```

**values-es/strings.xml:**
```xml
<string name="auth_error_email_empty">El correo electrónico no puede estar vacío</string>
<string name="auth_error_email_invalid">Por favor ingresa un correo electrónico válido</string>
<string name="auth_error_password_empty">La contraseña no puede estar vacía</string>
<string name="auth_error_password_short">La contraseña debe tener al menos 6 caracteres</string>
<string name="auth_error_passwords_mismatch">Las contraseñas no coinciden</string>
<string name="auth_error_biometric_not_configured">Inicio de sesión biométrico no configurado</string>
```

**Implementation:**
AuthViewModel needs access to string resources, which requires passing a StringProvider or using Compose's stringResource in a Composable context.

---

### Issue #4: No PIN Lock Awareness

**Problem:**
The SDK has no concept of PIN lock authentication. This means:
- Apps must implement PIN + biometric mutual exclusion themselves
- No guidance from SDK on how these should interact
- Every app will implement this differently

**Evidence from BiometricsLogin:**
We had to implement ALL of this outside the SDK:
- PinManager
- PinViewModel
- PinScreen
- Mutual exclusion logic
- Multiple bugs from interaction between PIN and biometric

**Recommendation:**
Either:
1. **Document Best Practices** - Clear guidance on implementing PIN alongside biometric
2. **Add PIN Module to SDK** - Optional `supabase-auth-pin` module
3. **Add Security Mode Enum** - SDK-aware of different security modes

**Example Security Mode Approach:**
```kotlin
enum class SecurityMode {
    NONE,           // Just username/password
    BIOMETRIC,      // Biometric authentication
    PIN,            // PIN code
    BIOMETRIC_PIN   // Both (PIN as fallback)
}

data class SecurityConfig(
    val mode: SecurityMode,
    val biometricAvailable: Boolean,
    val pinEnabled: Boolean
)

// SDK could validate mutual exclusion
fun setSecurityMode(mode: SecurityMode) {
    require(mode != SecurityMode.BIOMETRIC_PIN || biometricAvailable) {
        "Biometric not available"
    }
}
```

---

### Issue #5: Error/Success Display Not Configurable

**Problem:**
Errors and success messages are always shown via Snackbar, which:
- Auto-dismisses quickly
- May not suit all app designs
- Cannot be customized

**Current Implementation (AuthScreen.kt:84-96):**
```kotlin
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
```

**Recommendation:**
Make message display configurable:
```kotlin
enum class MessageDisplayMode {
    SNACKBAR,       // Current behavior
    DIALOG,         // Modal dialog
    INLINE,         // Inline in form
    CALLBACK        // Let app handle display
}

@Composable
fun AuthScreen(
    viewState: AuthViewState,
    onAction: (AuthAction) -> Unit,
    messageDisplayMode: MessageDisplayMode = MessageDisplayMode.SNACKBAR,
    modifier: Modifier = Modifier
)
```

---

### Issue #6: Limited Customization Options

**Problem:**
The SDK UI has limited customization:
- Cannot hide "Continue without account"
- Cannot change biometric button position
- Cannot add custom fields (e.g., username)
- Cannot customize validation rules

**Recommendation:**
Add configuration object:
```kotlin
data class AuthScreenConfig(
    val showContinueWithoutAccount: Boolean = true,
    val showBiometricAtTop: Boolean = true,
    val minPasswordLength: Int = 6,
    val requireConfirmPasswordOnSignIn: Boolean = false,
    val showForgotPassword: Boolean = true,
    val customFields: List<CustomField> = emptyList()
)

data class CustomField(
    val key: String,
    val label: StringResource,
    val inputType: KeyboardType,
    val validation: (String) -> String?
)
```

---

## Architecture Recommendations

### 1. Clarify Responsibility Boundaries

| Responsibility | SDK | App |
|----------------|-----|-----|
| UI rendering | ✅ | |
| Form validation | ✅ | |
| API calls | ✅ (via interface) | ✅ (implements AuthService) |
| Navigation | | ✅ (via callbacks) |
| Biometric prompt | | ✅ (via callback) |
| **Credential storage** | ❌ **UNCLEAR** | ❌ **UNCLEAR** |
| **Remember me** | ❌ **MISSING** | ✅ (hacky workaround) |
| **PIN lock** | ❌ | ✅ (full implementation) |
| **Error localization** | ❌ **PARTIAL** | |

### 2. Proposed Interface Changes

```kotlin
interface AuthCallbacks {
    // Required
    suspend fun onAuthSuccess(userId: String, username: String)
    suspend fun onContinueWithoutAccount()

    // Biometric (required if biometric enabled)
    suspend fun onBiometricAuthRequested(username: String): BiometricAuthResult

    // NEW: Credential management
    suspend fun onCredentialsShouldBeSaved(username: String, password: String)
    fun getSavedCredentials(username: String): SavedCredentials?

    // NEW: Remember me
    suspend fun onRememberMeChanged(username: String, remember: Boolean)
    fun isUsernameRemembered(username: String): Boolean
    fun getRememberedUsername(): String?

    // Analytics (optional)
    suspend fun onSignInClick() {}
    suspend fun onSignInSuccess(method: String, userId: String) {}
    suspend fun onSignUpSuccess(method: String, userId: String) {}
}

data class SavedCredentials(
    val username: String,
    val password: String
)
```

### 3. Proposed BiometricConfig Changes

```kotlin
data class BiometricConfig(
    val enabled: Boolean = false,
    val available: Boolean = false,
    val savedUsername: String? = null,
    // NEW: SDK can check if credentials are available
    val hasStoredCredentials: Boolean = false
)
```

---

## Testing Findings from BiometricsLogin App

### Bugs Encountered Due to SDK Design

1. **Password not available for biometric** - Had to hardcode
2. **Save username not in SDK** - Built custom preference storage
3. **PIN + biometric mutual exclusion** - No SDK guidance, led to multiple bugs
4. **Biometric prompt on PIN screen** - Confusing because SDK doesn't know about PIN
5. **Sign out navigation** - SDK doesn't define where to go after sign out

### App Code That Should Be SDK Responsibility

```kotlin
// These should potentially be in SDK:

// 1. Saving username preference
preferencesManager.saveString(AuthPreferenceKeys.SAVED_USERNAME, username)

// 2. Saving password for biometric
preferencesManager.saveString("saved_password_$username", currentPassword)

// 3. Checking if biometric should be enabled
val hasSavedPassword = savedUsername?.let {
    preferencesManager.getString("saved_password_$it") != null
} ?: false
```

---

## Priority Action Items

### High Priority (Security/Functionality)

1. **🔴 Fix password storage responsibility**
   - Add callback for credential storage OR
   - SDK handles secure storage internally
   - Without this, biometric auth cannot work correctly

2. **🔴 Localize validation error messages**
   - Add to string resources
   - Translate to Spanish
   - Easy fix, high impact

### Medium Priority (UX/Consistency)

3. **🟡 Add "Remember me" checkbox**
   - Standard UX feature
   - Reduces app-side code
   - Makes BiometricConfig simpler

4. **🟡 Document PIN integration best practices**
   - How to implement PIN + biometric
   - Mutual exclusion patterns
   - Example code

### Low Priority (Enhancements)

5. **🟢 Add configuration options**
   - Hide/show "Continue without account"
   - Custom validation rules
   - Message display mode

6. **🟢 Consider PIN module**
   - Optional `supabase-auth-pin`
   - Standard PIN implementation
   - Works with auth-ui

---

## Conclusion

The SDK provides a solid foundation for authentication UI, but has critical gaps in:

1. **Credential management** - Password storage for biometric is completely undefined
2. **Localization** - Validation errors are not localized
3. **Security mode integration** - No awareness of PIN or other security methods

These issues cause every consuming app to:
- Implement workarounds
- Make inconsistent decisions
- Risk security vulnerabilities

**Recommended immediate action:** Fix password storage responsibility by adding `onCredentialsShouldBeSaved` callback or implementing internal secure storage.

---

## Appendix: Current String Resources

### English (values/strings.xml)
- 21 strings for UI
- 0 strings for validation errors

### Spanish (values-es/strings.xml)
- 21 strings for UI (all translated)
- 0 strings for validation errors

### Missing Strings
```
auth_error_email_empty
auth_error_email_invalid
auth_error_password_empty
auth_error_password_short
auth_error_passwords_mismatch
auth_error_biometric_not_configured
auth_success_account_created
auth_success_reset_email_sent
```

---

**Document Version:** 1.0
**Last Updated:** 2026-01-08
