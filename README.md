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

### Publishing to Maven Local

To publish all modules to your local Maven repository for testing:

```bash
./gradlew publishToMavenLocal
```

To publish a specific module:

```bash
./gradlew :supabase-core:publishToMavenLocal
```

#### What Gets Published

Each module publishes **4 separate artifacts** per platform:

1. **kotlinMultiplatform** - KMP metadata (`supabase-core`)
2. **Android** - AAR library (`supabase-core-android`)
3. **iOS arm64** - Kotlin/Native library (`supabase-core-iosarm64`)
4. **iOS simulator arm64** - Kotlin/Native library (`supabase-core-iossimulatorarm64`)

Published artifacts location: `~/.m2/repository/com/dallaslabs/sdk/`

#### Consuming Locally Published Artifacts

**In a KMP Project:**
```kotlin
// settings.gradle.kts
repositories {
    mavenLocal()
    mavenCentral()
}

// build.gradle.kts
dependencies {
    implementation("com.dallaslabs.sdk:supabase-core:1.2.1")
    implementation("com.dallaslabs.sdk:supabase-auth:1.2.1")
    implementation("com.dallaslabs.sdk:supabase-auth-ui:1.2.1") // Compose Multiplatform only
}
```

**In a Native Android Project:**
```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.dallaslabs.sdk:supabase-core-android:1.2.1")
    implementation("com.dallaslabs.sdk:supabase-auth-android:1.2.1")
    // Note: supabase-auth-ui requires Compose Multiplatform
}
```

**In a Native iOS Project:**

iOS frameworks are built in `build/bin/iosArm64/releaseFramework/` and `build/bin/iosSimulatorArm64/releaseFramework/`. Build them with:

```bash
./gradlew :supabase-core:linkReleaseFrameworkIosArm64
./gradlew :supabase-core:linkReleaseFrameworkIosSimulatorArm64
```

Then add the `.framework` files to your Xcode project.

**Important Notes:**
- `supabase-auth-ui` contains Compose Multiplatform UI and requires a KMP app with Compose support
- Other modules (`supabase-core`, `supabase-auth`, `supabase-db`, `supabase-koin`) work with both native and KMP apps

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