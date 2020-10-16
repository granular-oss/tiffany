import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.applyCommonMultiplatform() {

    apply(plugin = "org.jetbrains.kotlin.multiplatform")

    configure<KotlinMultiplatformExtension> {
        targets.apply {
            add(jvm())
            add(iosX64("native"))
            add(iosArm64())
            add(iosArm32())
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

            listOf("iosArm64", "iosArm32").forEach {
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

    // A hack to copy resources for native tests
    val copyResourcesForNative = tasks.register<Copy>("copyResourcesForNative") {
        from("$projectDir/src/commonTest/resources/")
        into("$buildDir/bin/native/debugTest/")
    }

    tasks.named("nativeTest") {
        dependsOn(copyResourcesForNative)
    }

}