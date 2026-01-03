import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

object Android {
    const val MIN_SDK = 24
    const val COMPILE_SDK = 35
    const val TARGET_SDK = 35
}

fun Project.configurePublishing(moduleName: String) {
    extensions.configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/erikg84/supabase-sdk")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }

        publications {
            withType<MavenPublication> {
                groupId = "com.dallaslabs.sdk"
                artifactId = when (name) {
                    "kotlinMultiplatform" -> moduleName
                    else -> "$moduleName-${name.lowercase()}"
                }

                pom {
                    name.set(moduleName)
                    description.set("Kotlin Multiplatform Supabase SDK")
                    url.set("https://github.com/erikg84/supabase-sdk")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("erikg84")
                            name.set("Erik G")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/erikg84/supabase-sdk.git")
                        developerConnection.set("scm:git:ssh://github.com:erikg84/supabase-sdk.git")
                        url.set("https://github.com/erikg84/supabase-sdk")
                    }
                }
            }
        }
    }
}
