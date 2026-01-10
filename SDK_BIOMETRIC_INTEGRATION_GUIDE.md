# Supabase Auth SDK - Biometric Integration Guide

## Architecture Overview

### Responsibilities

**SDK** (`supabase-auth-ui`):
- Provides enhanced Auth UI with biometric option
- Triggers biometric authentication when button clicked
- Calls app callback `onBiometricAuthRequested()`
- Handles backend authentication with retrieved password

**App** (e.g., `cabinetdoors-cmp`):
- Manages biometric enrollment in Settings screen
- Stores username/password in encrypted SharedPreferences
- Triggers system biometric prompt (via MOKO or native)
- Returns password to SDK on successful biometric auth

### Data Flow

```
┌─────────────────────────────────────────────────────┐
│  SDK Auth Screen                                    │
│  ┌───────────────────────────────────────────┐     │
│  │  [👆 Login with Biometric]                │     │
│  │   Logging in as user@example.com          │     │
│  └───────────────────────────────────────────┘     │
│                    │                                │
│                    ▼                                │
│    AuthAction.BiometricAuthClicked                 │
│                    │                                │
│                    ▼                                │
│  onBiometricAuthRequested(username) ────────────┐  │
└─────────────────────────────────────────────────│──┘
                                                   │
                                                   ▼
┌──────────────────────────────────────────────────────┐
│  App (AuthCallbacks Implementation)                  │
│                                                      │
│  1. Trigger system biometric prompt                 │
│  2. On success: retrieve encrypted password         │
│  3. Return BiometricAuthResult.Success(password)    │
│                                                      │
│                    │                                │
│                    ▼                                │
│     ┌──────────────────────────────┐               │
│     │  Encrypted SharedPreferences │               │
│     │  - username                  │               │
│     │  - encrypted password        │               │
│     └──────────────────────────────┘               │
└──────────────────────────────────────────────────────┘
                    │
                    ▼
            Return password to SDK
                    │
                    ▼
┌──────────────────────────────────────────────────────┐
│  SDK: Authenticate with backend using password      │
│  onAuthSuccess(userId, username)                    │
└──────────────────────────────────────────────────────┘
```

---

## Step 1: Update AuthCallbacks Implementation

### In Your App's AuthViewModel

```kotlin
// In cabinetdoors-cmp app
class AppAuthCallbacks(
    private val biometricManager: BiometricAuthManager,
    private val securePrefs: SecurePreferences,
    private val navigator: AppRouter
) : AuthCallbacks {

    override suspend fun onAuthSuccess(userId: String, username: String) {
        // Save username for biometric login (App responsibility)
        securePrefs.saveUsername(username)

        // Navigate to home
        navigator.navigateTo(Route.Home, clearStack = true)

        // Analytics
        trackEvent("auth_success", userId)
    }

    override suspend fun onBiometricAuthRequested(username: String): BiometricAuthResult {
        // Trigger system biometric prompt (App responsibility)
        return try {
            // Use MOKO or native biometric manager
            biometricManager.authenticate(
                title = "Login Required",
                subtitle = "Verify your identity",
                description = "Use biometrics to login as $username"
            )

            when (val result = biometricManager.authState.value) {
                is BiometricAuthManager.AuthState.Success -> {
                    // Retrieve encrypted password
                    val password = securePrefs.getPassword(username)
                    if (password != null) {
                        BiometricAuthResult.Success(password)
                    } else {
                        BiometricAuthResult.Failed("Password not found")
                    }
                }
                is BiometricAuthManager.AuthState.Cancelled -> {
                    BiometricAuthResult.Cancelled
                }
                else -> {
                    BiometricAuthResult.Failed("Authentication failed")
                }
            }
        } catch (e: Exception) {
            BiometricAuthResult.Failed(e.message)
        }
    }

    override suspend fun onContinueWithoutAccount() {
        navigator.navigateTo(Route.Home, clearStack = true)
    }

    override suspend fun onSignInSuccess(method: String, userId: String) {
        trackEvent("sign_in_success", mapOf("method" to method, "userId" to userId))
    }
}
```

---

## Step 2: Create SecurePreferences Implementation

### Android Implementation

```kotlin
// androidMain
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.content.Context

class AndroidSecurePreferences(context: Context) : SecurePreferences {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "biometric_auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun saveUsername(username: String) {
        sharedPreferences.edit()
            .putString(KEY_USERNAME, username)
            .apply()
    }

    override fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    override fun savePassword(username: String, password: String) {
        sharedPreferences.edit()
            .putString("password_$username", password)
            .apply()
    }

    override fun getPassword(username: String): String? {
        return sharedPreferences.getString("password_$username", null)
    }

    override fun clearCredentials() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val KEY_USERNAME = "saved_username"
    }
}
```

### iOS Implementation

```kotlin
// iosMain
import platform.Foundation.NSUserDefaults
import platform.Security.*

class IOSSecurePreferences : SecurePreferences {

    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun saveUsername(username: String) {
        userDefaults.setObject(username, forKey = "saved_username")
    }

    override fun getUsername(): String? {
        return userDefaults.stringForKey("saved_username")
    }

    override fun savePassword(username: String, password: String) {
        // Use iOS Keychain for password storage
        saveToKeychain(key = "password_$username", value = password)
    }

    override fun getPassword(username: String): String? {
        return retrieveFromKeychain(key = "password_$username")
    }

    override fun clearCredentials() {
        userDefaults.removeObjectForKey("saved_username")
        // Clear keychain entries
    }

    private fun saveToKeychain(key: String, value: String) {
        // iOS Keychain implementation
        // Use SecItemAdd/SecItemUpdate
    }

    private fun retrieveFromKeychain(key: String): String? {
        // Use SecItemCopyMatching
        return null // Implementation needed
    }
}
```

### Common Interface

```kotlin
// commonMain
interface SecurePreferences {
    fun saveUsername(username: String)
    fun getUsername(): String?
    fun savePassword(username: String, password: String)
    fun getPassword(username: String): String?
    fun clearCredentials()
}
```

---

## Step 3: Configure BiometricConfig in AuthViewModel

```kotlin
// In your app's ViewModel that manages AuthScreen

class MyAuthViewModel(
    private val supabaseAuth: SupabaseAuth,
    private val securePrefs: SecurePreferences,
    private val biometricManager: BiometricAuthManager
) : BaseViewModel<MyAuthViewState, MyAuthAction>(...) {

    init {
        checkBiometricSetup()
    }

    private fun checkBiometricSetup() {
        launch {
            val biometricAvailable = biometricManager.isBiometricAvailable.value ?: false
            val savedUsername = securePrefs.getUsername()
            val biometricEnabled = savedUsername != null // User has previously saved credentials

            // Update SDK auth state with biometric config
            updateAuthViewState {
                it.copy(
                    biometricConfig = BiometricConfig(
                        enabled = biometricEnabled,
                        available = biometricAvailable,
                        savedUsername = savedUsername
                    )
                )
            }
        }
    }

    override fun onAction(action: MyAuthAction) {
        when (action) {
            is MyAuthAction.OnAuthSuccess -> {
                // User successfully signed in with email/password
                val username = action.username
                val password = action.password

                // Ask user if they want to enable biometric login
                showBiometricEnrollmentDialog(username, password)
            }
        }
    }

    private suspend fun showBiometricEnrollmentDialog(username: String, password: String) {
        // Show dialog: "Enable biometric login for faster access?"
        val userAccepted = /* show dialog and get result */

        if (userAccepted) {
            // Save credentials for future biometric login
            securePrefs.saveUsername(username)
            securePrefs.savePassword(username, password)

            // Update biometric config
            checkBiometricSetup()
        }
    }
}
```

---

## Step 4: Settings Screen - Enable/Disable Biometric

```kotlin
@Composable
fun SettingsScreen(
    securePrefs: SecurePreferences,
    biometricManager: BiometricAuthManager
) {
    val username = remember { securePrefs.getUsername() }
    val biometricEnabled = username != null
    var showEnableDialog by remember { mutableStateOf(false) }

    Column {
        Text("Security Settings", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // Biometric toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Biometric Login", style = MaterialTheme.typography.titleMedium)
                if (biometricEnabled) {
                    Text("Enabled for $username", style = MaterialTheme.typography.bodySmall)
                }
            }

            Switch(
                checked = biometricEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        showEnableDialog = true
                    } else {
                        // Disable biometric
                        securePrefs.clearCredentials()
                    }
                }
            )
        }
    }

    // Enable biometric dialog
    if (showEnableDialog) {
        BiometricEnrollmentDialog(
            onConfirm = { password ->
                // User entered their password
                securePrefs.savePassword(username, password)
                showEnableDialog = false
            },
            onDismiss = { showEnableDialog = false }
        )
    }
}

@Composable
fun BiometricEnrollmentDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enable Biometric Login") },
        text = {
            Column {
                Text("Enter your password to enable biometric login")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(password) },
                enabled = password.isNotBlank()
            ) {
                Text("Enable")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
```

---

## Step 5: Test Locally with AAR

### Build SDK AAR

```bash
cd /Users/erikgutierrez/IdeaProjects/supabase-sdk

# Build release AAR
./gradlew :supabase-auth-ui:assembleRelease

# AAR location:
# supabase-auth-ui/build/outputs/aar/supabase-auth-ui-release.aar
```

### Copy AAR to Your App

```bash
# Copy to cabinetdoors-cmp libs folder
mkdir -p /Users/erikgutierrez/AndroidStudioProjects/cabinetdoors-cmp/app/libs
cp supabase-auth-ui/build/outputs/aar/supabase-auth-ui-release.aar \
   /Users/erikgutierrez/AndroidStudioProjects/cabinetdoors-cmp/app/libs/
```

### Configure App build.gradle.kts

```kotlin
// In cabinetdoors-cmp/app/build.gradle.kts

android {
    // ... existing config
}

dependencies {
    // Local AAR for testing
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))

    // Or specific file
    implementation(files("libs/supabase-auth-ui-release.aar"))

    // SDK dependencies (must be included)
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // Your biometric implementation
    implementation("dev.icerock.moko:biometry-compose:0.4.0")
    // Or androidx.biometric
}
```

### Sync and Test

1. **Sync Gradle** - File → Sync Project with Gradle Files
2. **Clean Build** - Build → Clean Project
3. **Rebuild** - Build → Rebuild Project
4. **Run** - Test biometric integration

---

## API Summary

### Updated SDK Types

```kotlin
// BiometricConfig - passed to SDK
data class BiometricConfig(
    val enabled: Boolean = false,      // App has biometric enabled in settings
    val available: Boolean = false,    // Device has biometric capability
    val savedUsername: String? = null  // Username to display
)

// AuthCallbacks - implement in app
interface AuthCallbacks {
    suspend fun onAuthSuccess(userId: String, username: String)  // ← Updated with username
    suspend fun onBiometricAuthRequested(username: String): BiometricAuthResult  // ← New
    suspend fun onContinueWithoutAccount()
}

// BiometricAuthResult - return from app
sealed interface BiometricAuthResult {
    data class Success(val password: String) : BiometricAuthResult
    data class Failed(val reason: String?) : BiometricAuthResult
    data object Cancelled : BiometricAuthResult
}
```

### Usage Example

```kotlin
// In your app
val authCallbacks = remember {
    AppAuthCallbacks(
        biometricManager = biometricManager,
        securePrefs = securePrefs,
        navigator = navigator
    )
}

AuthScreen(
    viewState = authViewModel.viewState.collectAsState().value,
    onAction = authViewModel::onAction,
    modifier = Modifier.fillMaxSize()
)
```

---

## Security Best Practices

### ✅ DO
1. **Encrypt passwords** - Use EncryptedSharedPreferences (Android) / Keychain (iOS)
2. **Clear on logout** - Call `securePrefs.clearCredentials()` on sign out
3. **Verify biometric** - Always trigger biometric prompt before returning password
4. **Handle errors** - Return `BiometricAuthResult.Failed` on any error
5. **Test lockout** - Handle biometric lockout scenarios

### ❌ DON'T
1. **Store plain text passwords** - Always encrypt
2. **Skip biometric prompt** - Never return password without biometric auth
3. **Log passwords** - Don't log sensitive data
4. **Share preferences** - Keep separate from regular SharedPreferences
5. **Store on backend** - Passwords should only be on device

---

## Testing Checklist

- [ ] Build SDK AAR successfully
- [ ] Copy AAR to app/libs
- [ ] App builds with AAR dependency
- [ ] Biometric option shows when enabled
- [ ] Biometric prompt triggers on button click
- [ ] Password retrieved from secure storage
- [ ] Backend auth succeeds with retrieved password
- [ ] Settings toggle enables/disables biometric
- [ ] Biometric option hidden when disabled
- [ ] Works on both Android and iOS
- [ ] Encrypted storage verified
- [ ] Error handling tested (wrong biometric, cancelled, etc.)

---

## Troubleshooting

### AAR not found
```
Error: Could not find supabase-auth-ui-release.aar
```
**Solution:** Verify AAR exists in `app/libs/` directory

### BiometricConfig not updating
```
Biometric button not showing
```
**Solution:** Ensure you're calling `checkBiometricSetup()` after saving credentials

### Password not retrieved
```
BiometricAuthResult.Failed("Password not found")
```
**Solution:** Verify password was saved with `securePrefs.savePassword()`

### Biometric prompt not showing
```
onBiometricAuthRequested called but no prompt
```
**Solution:** Check biometric manager initialization and MOKO integration

---

## Migration from Old SDK

If you're upgrading from the previous SDK version:

```kotlin
// Old callback
override suspend fun onAuthSuccess(userId: String) { ... }

// New callback (username added)
override suspend fun onAuthSuccess(userId: String, username: String) {
    // Now you have username for secure storage
    securePrefs.saveUsername(username)
}

// New callback (biometric support)
override suspend fun onBiometricAuthRequested(username: String): BiometricAuthResult {
    // Implement biometric flow
}
```

---

For questions or issues, check the SDK documentation or example implementation in the cabinetdoors-cmp project.
