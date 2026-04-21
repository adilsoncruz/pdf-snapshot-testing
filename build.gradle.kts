import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    base
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.jlleitschuh.gradle.ktlint") apply false
    id("io.gitlab.arturbosch.detekt") apply false
}

allprojects {
    group = "com.github.adilsoncruz"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "jacoco")

    tasks.register("wrapper") {
        group = "build setup"
        description = "Delegates wrapper generation to the root project."
        dependsOn(rootProject.tasks.named("wrapper"))
    }

    if (tasks.findByName("prepareKotlinBuildScriptModel") == null) {
        tasks.register("prepareKotlinBuildScriptModel") {
            group = "ide"
            description = "Compatibility task for IDE import on subprojects."
        }
    }

    extensions.configure<DetektExtension> {
        toolVersion = "1.23.7"
        buildUponDefaultConfig = true
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(21)
            explicitApi()
        }
    }
}

tasks.withType<Test>().configureEach {
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.withType<JacocoReport>().configureEach {
    dependsOn(tasks.withType<Test>())

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.withType<JacocoCoverageVerification>().configureEach {
    dependsOn(tasks.withType<Test>())

    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "95.00".toBigDecimal()
            }
        }
    }
}


tasks.register("classes") {
    group = "build"
    description = "Assembles classes from all subprojects."
    dependsOn(subprojects.map { "${it.path}:classes" })
}

tasks.register("testClasses") {
    group = "verification"
    description = "Assembles test classes from all subprojects."
    dependsOn(subprojects.map { "${it.path}:testClasses" })
}

tasks.register("coverage") {
    group = "verification"
    description = "Runs tests and validates JaCoCo coverage in all subprojects."
    dependsOn(subprojects.flatMap { listOf("${it.path}:test", "${it.path}:jacocoTestReport", "${it.path}:jacocoTestCoverageVerification") })
}

tasks.register("coverageCore") {
    group = "verification"
    description = "Runs tests and validates JaCoCo coverage for :pdf-snapshot-core only."
    dependsOn(
        listOf(
            ":pdf-snapshot-core:test",
            ":pdf-snapshot-core:jacocoTestReport",
            ":pdf-snapshot-core:jacocoTestCoverageVerification",
        ),
    )
}
