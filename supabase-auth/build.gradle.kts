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
            baseName = "supabase-auth"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":supabase-core"))
            implementation(libs.supabase.gotrue)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

android {
    namespace = "com.dallaslabs.sdk.supabase.auth"
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
