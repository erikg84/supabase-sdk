# Supabase SDK

A Kotlin Multiplatform Supabase SDK monorepo for JVM and iOS platforms.

## Modules

- **supabase-core**: Client setup and configuration
- **supabase-db**: PostgREST queries and FilterBuilder
- **supabase-auth**: Authentication wrapper
- **supabase-koin**: Koin dependency injection module
- **supabase-auth-ui**: Compose Multiplatform authentication screens

## Dependencies

- Kotlin 2.2.0
- Supabase-kt 2.6.1
- Koin 4.1.1
- Compose Multiplatform 1.7.3
- Ktor 2.3.12

## Building

```bash
./gradlew build
```

## Publishing

The SDK is configured to publish to GitHub Packages under `com.dallaslabs.sdk`.

```bash
./gradlew publish
```

## Android Support

Android support requires the Android Gradle Plugin and Google Maven repository access. To add Android support:

1. Add `id("com.android.library")` to each module's build.gradle.kts
2. Configure `androidTarget` in the Kotlin Multiplatform setup
3. Add Android-specific dependencies

## Usage

### Core Module

```kotlin
import com.dallaslabs.sdk.supabase.core.*

val config = SupabaseConfig(
    url = "https://your-project.supabase.co",
    apiKey = "your-api-key"
)

val client = createSupabaseClient(config)
```

### Database Module

```kotlin
import com.dallaslabs.sdk.supabase.db.*

val database = SupabaseDatabase(client)
val users = database.from("users")
```

### Authentication Module

```kotlin
import com.dallaslabs.sdk.supabase.auth.*

val auth = SupabaseAuth(client)
auth.signInWithEmail("user@example.com", "password")
```

### Koin Module

```kotlin
import com.dallaslabs.sdk.supabase.koin.*
import org.koin.core.context.startKoin

startKoin {
    modules(supabaseModule(
        url = "https://your-project.supabase.co",
        apiKey = "your-api-key"
    ))
}
```

## License

Apache 2.0