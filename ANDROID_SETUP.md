# Adding Android Support

This guide explains how to add Android support to the Supabase SDK monorepo. The project is currently configured for JVM and iOS targets. Android support requires access to Google Maven repository for Android Gradle Plugin and Android-specific dependencies.

## Prerequisites

- Access to Google Maven repository (https://maven.google.com)
- Android SDK installed
- JDK 17 or later

## Steps to Add Android Support

### 1. Update build.gradle.kts

Add the Android Gradle Plugin to the root `build.gradle.kts`:

```kotlin
plugins {
    kotlin("multiplatform") version "2.2.0" apply false
    id("com.android.library") version "8.5.2" apply false
    id("org.jetbrains.compose") version "1.7.3" apply false
}

allprojects {
    group = findProperty("GROUP") as String
    version = findProperty("VERSION") as String
    
    repositories {
        mavenCentral()
        google()  // Add this
    }
}
```

### 2. Update settings.gradle.kts

Ensure Google repository is in pluginManagement:

```kotlin
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

### 3. Update Module Build Files

For each module (supabase-core, supabase-db, supabase-auth, supabase-koin, supabase-auth-ui), update the `build.gradle.kts`:

#### Add Android Plugin

```kotlin
plugins {
    kotlin("multiplatform")
    id("com.android.library")  // Add this
    `maven-publish`
}
```

#### Configure Android Target

```kotlin
kotlin {
    explicitApi()

    androidTarget {
        publishLibraryVariants("release")
    }

    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "module-name"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // ...
        }

        androidMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
            // Add other Android-specific dependencies
        }
    }
}
```

#### Add Android Configuration Block

```kotlin
android {
    namespace = "com.dallaslabs.sdk.supabase.module"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

### 4. Module-Specific Android Dependencies

#### supabase-core

```kotlin
androidMain.dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("io.ktor:ktor-client-android:2.3.12")
}
```

#### supabase-koin

```kotlin
androidMain.dependencies {
    implementation("io.insert-koin:koin-android:4.1.1")
}
```

#### supabase-auth-ui

```kotlin
androidMain.dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}

android {
    // ...
    buildFeatures {
        compose = true
    }
}
```

### 5. Create Android Source Sets

For modules with Android-specific code, create the androidMain directories:

```bash
mkdir -p module-name/src/androidMain/kotlin/com/dallaslabs/sdk/supabase/module
```

### 6. Build and Test

After making these changes, you should be able to build the Android targets:

```bash
./gradlew build
./gradlew :supabase-core:assembleRelease
```

## Troubleshooting

### Google Maven Repository Access

If you encounter errors like "Could not resolve plugin artifact 'com.android.library:...'", ensure:

1. Your network allows access to `https://maven.google.com` and `https://dl.google.com`
2. The Google repository is properly configured in both `pluginManagement` and `dependencyResolutionManagement`

### Minimum SDK Version

The project is configured with `minSdk = 24` (Android 7.0). This is a requirement specified in the project requirements. To change this, update the `Android.MIN_SDK` constant in `buildSrc/src/main/kotlin/BuildHelpers.kt`.

### Compilation Errors

If you encounter compilation errors after adding Android support:

1. Sync the project: `./gradlew --refresh-dependencies`
2. Clean and rebuild: `./gradlew clean build`
3. Check that all Android-specific imports are available in `androidMain` source sets

## Testing Android Builds

To test Android-specific functionality:

1. Create an Android sample app in a separate module
2. Add dependencies on the SDK modules
3. Run on an Android emulator or device

Example Android app dependency:

```kotlin
dependencies {
    implementation(project(":supabase-core"))
    implementation(project(":supabase-auth"))
    implementation(project(":supabase-koin"))
}
```

## Publishing Android Artifacts

Android artifacts will be automatically published when running:

```bash
./gradlew publish
```

This will publish AAR files for Android alongside JAR files for JVM and framework files for iOS.
