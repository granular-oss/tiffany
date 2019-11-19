import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering

fun Project.applyKtLint() {
    val ktlintConfig by configurations.creating

    dependencies {
        ktlintConfig("com.pinterest:ktlint:0.32.0")
    }

    val ktlint by tasks.registering(JavaExec::class) {
        group = "verification"
        description = "Check Kotlin code style."
        classpath = ktlintConfig
        main = "com.pinterest.ktlint.Main"
        args = listOf("src/**/*.kt")
    }

    val ktlintformat by tasks.registering(JavaExec::class) {
        group = "formatting"
        description = "Fix Kotlin code style deviations."
        classpath = ktlintConfig
        main = "com.pinterest.ktlint.Main"
        args = listOf("-F", "src/**/*.kt")
    }

    tasks.getByName("check") {
        dependsOn(ktlint)
    }
}