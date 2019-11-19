import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.konan.target.HostManager

fun Project.applyCommonMultiplatform() {

    apply(plugin = "org.jetbrains.kotlin.multiplatform")

    configure<KotlinMultiplatformExtension> {
        targets.apply {
            add(jvm())
            add(iosX64("native"){
                binaries {
                    executable {
                        freeCompilerArgs.add("-opt")
                    }
                }
            })
            add(iosArm64())
            add(iosArm32())
        }

        sourceSets.apply {

            named("commonMain") {
                dependencies {
                    implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
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
                    implementation("org.jetbrains.kotlin:kotlin-stdlib")
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


    // A hack to copy resources for JVM tests
    tasks.register<Copy>("copyResourcesForJvm") {
        from("$projectDir/src/commonTest/resources/")
        into("$buildDir/classes/kotlin/jvm/test/")
    }

    tasks.named("jvmTest") {
        dependsOn(tasks.getByName("copyResourcesForJvm"))
    }


    // iOS Test Runner
    if (HostManager.hostIsMac) {

        // A hack to copy resources for native tests
        val copyResourcesForNative = tasks.register<Copy>("copyResourcesForNative") {
            from("$projectDir/src/commonTest/resources/")
            into("$buildDir/bin/native/debugTest/")
        }

        val linkDebugTestNative = tasks.getByName("linkDebugTestNative", KotlinNativeLink::class)

        val testIosSim = tasks.register<Exec>("iosTest") {
            group = "verification"
            dependsOn(linkDebugTestNative, copyResourcesForNative)
            executable = "xcrun"
            setArgs(
                listOf(
                    "simctl",
                    "spawn",
                    "-s",
                    "iPad Air 2",
                    linkDebugTestNative.outputFile.get()
                )
            )
        }

        tasks.getByName("check") {
            dependsOn(testIosSim)
        }
    }
}