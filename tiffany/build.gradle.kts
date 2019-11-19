import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

applyCommonMultiplatform()
applyMavenPublish()
applyKtLint()

configure<KotlinMultiplatformExtension> {
    sourceSets.apply {
        named("commonMain") {
            dependencies {
            }
        }
    }
}







