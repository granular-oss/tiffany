import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.plugins.signing.SigningExtension

fun Project.applyMavenPublish() {

    // Environment variables
    val repositoryUsername: String? = System.getenv("SONATYPE_USERNAME")
    val repositoryPassword: String? = System.getenv("SONATYPE_PASSWORD")

    val signingKeyId: String? = System.getenv("SIGNING_KEY_ID")
    val signingSecretKeyRingFile: String? = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
    val signingPassword: String? = System.getenv("SIGNING_PASSWORD")

    // pom
    val pomDescription = "A library for parsing Tagged Image File Format (Tiff) files"
    val gitUrl = "github.com/granular-oss/tiffany"
    val pomUrl = "https://$gitUrl"

    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    val versionName: String = property("VERSION_NAME") as String

    group = "ag.granular"
    version = versionName

    val isReleaseBuild = !versionName.contains("SNAPSHOT")

    val emptyJavadocJar = tasks.register<Jar>("emptyJavadocJar") {
        classifier = "javadoc"
    }

    val releaseRepositoryUrl: String = findProperty("RELEASE_REPOSITORY_URL") as String?
        ?: "https://oss.sonatype.org/service/local/staging/deploy/maven2/"

    val snapshotRepositoryUrl: String =
        findProperty("SNAPSHOT_REPOSITORY_URL") as String? ?: "https://oss.sonatype.org/content/repositories/snapshots/"


    configure<PublishingExtension> {
        publications.all {
            if (this is MavenPublication) {
                artifact(emptyJavadocJar.get())

                pom {
                    name.set(project.name)
                    description.set(pomDescription)

                    url.set(pomUrl)
                    licenses {
                        license {
                            name.set("The MIT License (MIT)")
                            url.set("https://raw.githubusercontent.com/granular-oss/tiffany/master/LICENSE")
                            distribution.set("repo")
                        }
                    }
                    scm {
                        url.set(pomUrl)
                        connection.set("scm:git:git://$gitUrl.git")
                        developerConnection.set("scm:git:ssh://git@$gitUrl.git")
                    }
                    developers {
                        developer {
                            id.set("lavruk")
                            name.set("Vladimir Lavruk")
                        }
                    }
                }
            }
        }

        repositories {
            maven {
                url = uri(if (isReleaseBuild) releaseRepositoryUrl else snapshotRepositoryUrl)
                credentials {
                    username = repositoryUsername
                    password = repositoryPassword
                }
            }
        }
    }

    allprojects {
        extra["signing.keyId"] = signingKeyId
        extra["signing.secretKeyRingFile"] = signingSecretKeyRingFile
        extra["signing.password"] = signingPassword
    }

    configure<SigningExtension> {
        isRequired = isReleaseBuild

        val publishing = the<PublishingExtension>()
        sign(publishing.publications)
    }
}
