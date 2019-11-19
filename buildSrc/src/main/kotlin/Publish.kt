import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

fun Project.applyMavenPublish() {

    apply(plugin = "maven-publish")

    group = "ag.granular"
    version  = property("VERSION") as String
}