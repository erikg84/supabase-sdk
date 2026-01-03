plugins {
    kotlin("multiplatform")
    `maven-publish`
}

kotlin {
    explicitApi()

    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "supabase-db"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":supabase-core"))
            implementation("io.github.jan-tennert.supabase:postgrest-kt:2.6.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
        }
    }
}

configurePublishing()
