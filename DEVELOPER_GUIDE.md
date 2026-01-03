# Supabase SDK Monorepo - Developer Guide

## Project Structure

```
supabase-sdk/
├── supabase-core/          # Core Supabase client configuration
├── supabase-db/            # PostgREST database operations
├── supabase-auth/          # Authentication wrapper
├── supabase-koin/          # Koin dependency injection
├── supabase-auth-ui/       # Compose Multiplatform UI components
├── buildSrc/               # Shared build logic
├── gradle/                 # Gradle wrapper and version catalog
└── .github/workflows/      # CI/CD workflows
```

## Architecture

This is a Kotlin Multiplatform project targeting:
- **JVM**: For server-side and desktop applications
- **iOS**: Native iOS applications (arm64, x64, simulator arm64)
- **Android**: Mobile applications (requires additional setup - see ANDROID_SETUP.md)

### Module Dependencies

```
supabase-auth-ui
    ↓
supabase-auth ──┐
                │
supabase-db ────┼──→ supabase-core
                │
supabase-koin ──┘
```

## Module Details

### supabase-core

**Purpose**: Provides the foundational Supabase client setup and configuration.

**Key Classes**:
- `SupabaseConfig`: Configuration data class for client setup
- `createSupabaseClient()`: Factory function to create configured client

**Dependencies**:
- `io.github.jan-tennert.supabase:postgrest-kt:2.6.1`
- `io.github.jan-tennert.supabase:gotrue-kt:2.6.1`
- `io.ktor:ktor-client-core:2.3.12`

**Example Usage**:
```kotlin
val config = SupabaseConfig(
    url = "https://your-project.supabase.co",
    apiKey = "your-anon-key"
)
val client = createSupabaseClient(config)
```

### supabase-db

**Purpose**: Wraps PostgREST functionality for database operations.

**Key Classes**:
- `SupabaseDatabase`: Main database operations wrapper
- `FilterBuilder`: Helper object for query filtering

**Dependencies**:
- api: `supabase-core`
- implementation: `postgrest-kt`

**Example Usage**:
```kotlin
val database = SupabaseDatabase(client)
val users = database.from("users").select()
```

### supabase-auth

**Purpose**: Provides authentication functionality.

**Key Classes**:
- `SupabaseAuth`: Authentication wrapper with email/password support

**Key Methods**:
- `signInWithEmail(email, password)`: Sign in with credentials
- `signUpWithEmail(email, password)`: Register new user
- `signOut()`: Sign out current user
- `currentUser()`: Get current user info
- `observeSessionStatus()`: Observe auth state changes
- `isAuthenticated()`: Check authentication status

**Dependencies**:
- api: `supabase-core`
- implementation: `gotrue-kt`

**Example Usage**:
```kotlin
val auth = SupabaseAuth(client)
val result = auth.signInWithEmail("user@example.com", "password")
result.onSuccess { user ->
    println("Signed in: ${user.email}")
}
```

### supabase-koin

**Purpose**: Provides Koin dependency injection modules for the SDK.

**Key Functions**:
- `supabaseModule(config: SupabaseConfig)`: Creates Koin module with config
- `supabaseModule(url: String, apiKey: String)`: Convenience function

**Provided Dependencies**:
- `SupabaseClient`
- `SupabaseDatabase`
- `SupabaseAuth`

**Dependencies**:
- api: `supabase-core`, `supabase-db`, `supabase-auth`
- implementation: `koin-core:4.1.1`

**Example Usage**:
```kotlin
startKoin {
    modules(supabaseModule(
        url = "https://your-project.supabase.co",
        apiKey = "your-anon-key"
    ))
}

// Later, inject dependencies
class MyRepository(
    private val auth: SupabaseAuth,
    private val database: SupabaseDatabase
)
```

### supabase-auth-ui

**Purpose**: Provides Compose Multiplatform UI components for authentication.

**Key Composables**:
- `SignInScreen`: Email/password sign-in UI
- `SignUpScreen`: User registration UI

**Dependencies**:
- api: `supabase-auth`
- implementation: Compose Multiplatform libraries

**Example Usage**:
```kotlin
@Composable
fun AuthFlow(auth: SupabaseAuth) {
    var showSignUp by remember { mutableStateOf(false) }
    
    if (showSignUp) {
        SignUpScreen(
            auth = auth,
            onSignUpSuccess = { /* Navigate to app */ },
            onNavigateToSignIn = { showSignUp = false }
        )
    } else {
        SignInScreen(
            auth = auth,
            onSignInSuccess = { /* Navigate to app */ },
            onNavigateToSignUp = { showSignUp = true }
        )
    }
}
```

## Building the Project

### Prerequisites

- JDK 17 or later
- Gradle 8.11 (included via wrapper)
- For iOS targets: Xcode with command-line tools

### Build All Modules

```bash
./gradlew build
```

### Build Specific Module

```bash
./gradlew :supabase-core:build
```

### Clean Build

```bash
./gradlew clean build
```

### Run Tests

```bash
./gradlew test
```

## Publishing

### Configuration

The project is configured to publish to GitHub Packages at:
- Group: `com.dallaslabs.sdk`
- Repository: `https://maven.pkg.github.com/erikg84/supabase-sdk`

### Environment Variables

Set these for publishing:
- `GITHUB_ACTOR`: Your GitHub username
- `GITHUB_TOKEN`: Personal access token with `write:packages` scope

### Publish Command

```bash
export GITHUB_ACTOR=your-username
export GITHUB_TOKEN=your-token
./gradlew publish
```

### GitHub Actions

Publishing is automated via `.github/workflows/publish.yml`:
- Triggers on release creation
- Can be manually triggered via workflow_dispatch
- Automatically sets credentials from GitHub secrets

## Version Management

The project version is managed in `gradle.properties`:

```properties
VERSION=0.1.0
```

To release a new version:
1. Update `VERSION` in `gradle.properties`
2. Commit the change
3. Create a GitHub release with matching tag
4. GitHub Actions will build and publish automatically

## Development Guidelines

### Code Style

- Use explicit API mode (all public APIs must have explicit visibility)
- Follow Kotlin coding conventions
- Document public APIs with KDoc

### Adding New Features

1. Create feature branch from `main`
2. Implement changes in appropriate module
3. Add/update tests
4. Update documentation
5. Submit pull request

### Module Guidelines

- Keep modules focused on single responsibility
- Use `api` for transitive dependencies that consumers need
- Use `implementation` for internal dependencies
- Maintain backwards compatibility within major versions

## Troubleshooting

### Build Failures

1. **"Unresolved reference"**: Check that dependencies are properly declared
2. **"Could not resolve"**: Verify repository access and dependency versions
3. **iOS targets disabled**: Expected on non-macOS systems, add `kotlin.native.ignoreDisabledTargets=true` to gradle.properties

### Dependency Issues

```bash
./gradlew dependencies --configuration jvmCompileClasspath
```

### Refresh Dependencies

```bash
./gradlew --refresh-dependencies build
```

## CI/CD

### Build Workflow

`.github/workflows/build.yml` runs on:
- Push to `main` or `develop`
- Pull requests

Steps:
1. Checkout code
2. Setup JDK 17
3. Cache Gradle files
4. Run build
5. Run tests

### Publish Workflow

`.github/workflows/publish.yml` runs on:
- Release creation
- Manual trigger

Steps:
1. Checkout code
2. Setup JDK 17
3. Cache Gradle files
4. Build all modules
5. Publish to GitHub Packages

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Update documentation
6. Submit a pull request

## License

Apache License 2.0 - See LICENSE file for details

## Support

For issues and questions:
- GitHub Issues: https://github.com/erikg84/supabase-sdk/issues
- Discussions: https://github.com/erikg84/supabase-sdk/discussions

## Version History

### 0.1.0 (Current)

- Initial release
- Core modules: core, db, auth, koin, auth-ui
- JVM and iOS support
- GitHub Packages publishing
- GitHub Actions CI/CD
