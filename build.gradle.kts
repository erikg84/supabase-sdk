plugins {
    kotlin("multiplatform") version "2.2.0" apply false
    id("org.jetbrains.compose") version "1.7.3" apply false
}

allprojects {
    group = findProperty("GROUP") as String
    version = findProperty("VERSION") as String
    
    repositories {
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
