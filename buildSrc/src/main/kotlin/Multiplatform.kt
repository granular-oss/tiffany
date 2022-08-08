import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import java.io.File

fun Project.applyCommonMultiplatform() {

    apply(plugin = "org.jetbrains.kotlin.multiplatform")

    configure<KotlinMultiplatformExtension> {
        targets.apply {
            add(jvm())
            add(iosX64("native"))
            add(iosArm64())
            add(iosArm32())
            add(iosSimulatorArm64())
        }

        sourceSets.apply {

            named("commonMain") {
                dependencies {
                }
            }
            named("commonTest") {
                dependencies {
                    implementation("org.jetbrains.kotlin:kotlin-test-common")
                    implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                }
            }
            named("nativeMain") {
                dependencies {
                }
            }

            named("jvmMain") {
                dependencies {
                }
            }

            named("jvmTest") {
                dependencies {
                    implementation("org.jetbrains.kotlin:kotlin-test")
                    implementation("org.jetbrains.kotlin:kotlin-test-junit")
                }
            }

            listOf("iosArm64", "iosArm32", "iosSimulatorArm64").forEach {
                getByName("${it}Main") {
                    dependsOn(getByName("nativeMain"))
                }
                getByName("${it}Test") {
                    dependsOn(getByName("nativeTest"))
                }
            }
        }

// TODO check all warnings
//        targets.all {
//            compilations.all {
//                kotlinOptions.allWarningsAsErrors = true
//            }
//        }
    }

    tasks.withType<KotlinNativeTest> {
        val frameworkDir = executable.parentFile

        // A hack to copy resources for native tests
        val copyResources = tasks.register<Copy>("copyResourcesFor${name.capitalize()}") {
            from("$projectDir/src/commonTest/resources/")
            into(frameworkDir)
        }
        dependsOn(copyResources)
    }

    tasks.withType<AbstractTestTask>().configureEach {
        testLogging {
            events("started", "skipped", "passed", "failed")
            setExceptionFormat("full")
            showStandardStreams = true
        }
    }

}