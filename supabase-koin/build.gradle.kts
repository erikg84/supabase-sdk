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
            baseName = "supabase-koin"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":supabase-core"))
            api(project(":supabase-db"))
            api(project(":supabase-auth"))
            implementation("io.github.jan-tennert.supabase:gotrue-kt:2.6.1")
            implementation("io.insert-koin:koin-core:4.1.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
        }
    }
}

configurePublishing()
