import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

object Android {
    const val MIN_SDK = 24
    const val COMPILE_SDK = 35
    const val TARGET_SDK = 35
}

fun Project.configurePublishing(publicationName: String = "maven") {
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
            create<MavenPublication>(publicationName) {
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()

                pom {
                    name.set(project.name)
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
