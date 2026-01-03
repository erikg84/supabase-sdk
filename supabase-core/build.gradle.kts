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
            baseName = "supabase-core"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation("io.github.jan-tennert.supabase:postgrest-kt:2.6.1")
            implementation("io.github.jan-tennert.supabase:gotrue-kt:2.6.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            implementation("io.ktor:ktor-client-core:2.3.12")
        }

        jvmMain.dependencies {
            implementation("io.ktor:ktor-client-cio:2.3.12")
        }

        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:2.3.12")
        }
    }
}

configurePublishing()
