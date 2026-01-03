plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    `maven-publish`
}

kotlin {
    explicitApi()

    androidTarget {
        publishLibraryVariants("release")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "supabase-koin"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":supabase-core"))
            api(project(":supabase-db"))
            api(project(":supabase-auth"))
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}

android {
    namespace = "com.dallaslabs.sdk.supabase.koin"
    compileSdk = Android.COMPILE_SDK

    defaultConfig {
        minSdk = Android.MIN_SDK
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

configurePublishing()
