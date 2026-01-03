rootProject.name = "supabase-sdk"

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

include(":supabase-core")
include(":supabase-db")
include(":supabase-auth")
include(":supabase-auth-ui")
include(":supabase-koin")
