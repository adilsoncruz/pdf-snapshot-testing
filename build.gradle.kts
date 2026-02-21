import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    base
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.jlleitschuh.gradle.ktlint") apply false
    id("io.gitlab.arturbosch.detekt") apply false
}

allprojects {
    group = "com.github.adilsoncruz.pdfsnapshottesting"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

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
